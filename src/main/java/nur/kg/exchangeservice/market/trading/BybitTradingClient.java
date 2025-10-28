package nur.kg.exchangeservice.market.trading;

import com.bybit.api.client.config.BybitApiConfig;
import com.bybit.api.client.domain.CategoryType;
import com.bybit.api.client.domain.TradeOrderType;
import com.bybit.api.client.domain.account.AccountType;
import com.bybit.api.client.domain.account.request.AccountDataRequest;
import com.bybit.api.client.domain.trade.PositionIdx;
import com.bybit.api.client.domain.trade.Side;
import com.bybit.api.client.domain.trade.TimeInForce;
import com.bybit.api.client.domain.trade.request.TradeOrderRequest;
import com.bybit.api.client.domain.trade.response.OrderResponse;
import com.bybit.api.client.restApi.BybitApiAsyncAccountRestClient;
import com.bybit.api.client.restApi.BybitApiAsyncTradeRestClient;
import com.bybit.api.client.service.BybitApiClientFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import nur.kg.domain.enums.Exchange;
import nur.kg.domain.enums.Symbol;
import nur.kg.domain.request.OrderRequest;
import nur.kg.exchangeservice.config.BybitProperties;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Log4j2
@Component
@RequiredArgsConstructor
public class BybitTradingClient implements TradingClient {

    private final BybitProperties properties;
    private final ObjectMapper mapper;

    @Override
    public Mono<OrderResponse> placeOrder(OrderRequest r) {
        return getBalance()
                .flatMap(balance -> {
                    log.info("Balance: {}, Order request: {}", balance, r);
                    if (balance.compareTo(BigDecimal.ZERO) <= 0) {
                        return Mono.error(new IllegalStateException("Insufficient balance"));
                    }

                    BigDecimal price = r.limitPrice();
                    if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
                        return Mono.error(new IllegalStateException("Limit price is required"));
                    }

                    BigDecimal qty = balance.multiply(BigDecimal.valueOf(0.01))
                            .divide(price, 6, RoundingMode.DOWN);

                    var req = TradeOrderRequest.builder()
                            .category(CategoryType.LINEAR)
                            .symbol(r.symbol().name())
                            .side(Side.valueOf(r.side().name()))
                            .orderType(TradeOrderType.valueOf(r.type().name()))
                            .qty(qty.toPlainString())
                            .price(price.toPlainString())
                            .timeInForce(TimeInForce.GOOD_TILL_CANCEL)
                            .positionIdx(PositionIdx.ONE_WAY_MODE)
                            .build();

                    return Mono.create(sink -> {
                        BybitApiAsyncTradeRestClient client = factory().newAsyncTradeRestClient();
                        client.createOrder(req, resp -> {
                            log.info("RESPONSE: {}", resp);
                            OrderResponse response = mapper.convertValue(resp, OrderResponse.class);
                            sink.success(response);
                        });
                    });
                });
    }

    private Mono<BigDecimal> getBalance() {
        return Mono.create(sink -> {
            BybitApiAsyncAccountRestClient client = factory().newAsyncAccountRestClient();
            client.getWalletBalance(AccountDataRequest.builder()
                            .accountType(AccountType.UNIFIED)
                            .build(),
                    resp -> {
                        try {
                            JsonNode root = mapper.valueToTree(resp);

                            BigDecimal found = BigDecimal.ZERO;
                            JsonNode list = root.path("result").path("list");
                            if (list.isArray() && !list.isEmpty()) {
                                found = findCoinInArray(list, "USDT");
                            }

                            if (found.compareTo(BigDecimal.ZERO) == 0) {
                                found = findCoinRecursively(root, "USDT");
                            }

                            sink.success(found);
                        } catch (Exception e) {
                            log.warn("Failed to parse wallet balance response: {}", resp, e);
                            sink.success(BigDecimal.ZERO);
                        }
                    });
        });
    }

    private BigDecimal findCoinInArray(JsonNode array, String coinName) {
        for (JsonNode item : array) {
            BigDecimal b = findCoinNode(item, coinName);
            if (b.compareTo(BigDecimal.ZERO) > 0) return b;
        }
        return BigDecimal.ZERO;
    }

    private BigDecimal findCoinRecursively(JsonNode node, String coinName) {
        if (node.isObject()) {
            BigDecimal b = findCoinNode(node, coinName);
            if (b.compareTo(BigDecimal.ZERO) > 0) return b;
        }
        if (node.isContainerNode()) {
            for (JsonNode child : node) {
                BigDecimal res = findCoinRecursively(child, coinName);
                if (res.compareTo(BigDecimal.ZERO) > 0) return res;
            }
        }
        return BigDecimal.ZERO;
    }

    private BigDecimal findCoinNode(JsonNode node, String coinName) {
        if (node.has("coin") && coinName.equalsIgnoreCase(node.path("coin").asText())) {

            String[] balanceFields = {"availableToWithdraw", "available", "available_balance", "free", "avail"};
            for (String f : balanceFields) {
                JsonNode v = node.path(f);
                if (!v.isMissingNode() && (v.isNumber() || v.isTextual())) {
                    try {
                        return new BigDecimal(v.asText());
                    } catch (Exception ignored) {
                    }
                }
            }
        }
        return BigDecimal.ZERO;
    }

    @Override
    public Mono<Boolean> cancelAll(Symbol symbol) {
        return Mono.create(sink -> {
            BybitApiAsyncTradeRestClient client = factory().newAsyncTradeRestClient();
            TradeOrderRequest cancelAllOrdersRequest = TradeOrderRequest.builder().symbol(symbol.name()).build();
            client.cancelAllOrder(cancelAllOrdersRequest, resp -> sink.success());
        });
    }

    private BybitApiClientFactory factory() {
        String domain = properties.domain().equalsIgnoreCase("MAINNET")
                ? BybitApiConfig.MAINNET_DOMAIN
                : BybitApiConfig.TESTNET_DOMAIN;
        return BybitApiClientFactory.newInstance(
                properties.apiKey(), properties.apiSecret(), domain, true);
    }

    @Override
    public Exchange exchange() {
        return null;
    }
}
