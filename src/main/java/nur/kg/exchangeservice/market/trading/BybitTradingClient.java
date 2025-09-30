package nur.kg.exchangeservice.market.trading;

import com.bybit.api.client.config.BybitApiConfig;
import com.bybit.api.client.domain.CategoryType;
import com.bybit.api.client.domain.TradeOrderType;
import com.bybit.api.client.domain.trade.PositionIdx;
import com.bybit.api.client.domain.trade.Side;
import com.bybit.api.client.domain.trade.TimeInForce;
import com.bybit.api.client.domain.trade.request.TradeOrderRequest;
import com.bybit.api.client.domain.trade.response.OrderResponse;
import com.bybit.api.client.service.BybitApiClientFactory;
import lombok.RequiredArgsConstructor;
import nur.kg.domain.enums.Exchange;
import nur.kg.domain.enums.Symbol;
import nur.kg.domain.request.OrderRequest;
import nur.kg.exchangeservice.config.BybitProperties;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class BybitTradingClient implements TradingClient {

    private final BybitProperties properties;

    @Override
    public Mono<OrderResponse> placeOrder(OrderRequest r) {
        var req = TradeOrderRequest.builder()
                .category(CategoryType.LINEAR)
                .symbol(r.symbol().name())
                .side(Side.valueOf(r.side().name()))
                .orderType(TradeOrderType.valueOf(r.type().name()))
                .qty(r.qty().toPlainString())
                .price(r.limitPrice() != null ? r.limitPrice().toPlainString() : null)
                .timeInForce(TimeInForce.GOOD_TILL_CANCEL)
                .positionIdx(PositionIdx.ONE_WAY_MODE)
                .build();

        return Mono.create(sink -> {
            var async = factory().newAsyncTradeRestClient();
            async.createOrder(req, resp -> sink.success());
        });
    }

    private BybitApiClientFactory factory() {
        var domain = properties.domain().equalsIgnoreCase("MAINNET")
                ? BybitApiConfig.MAINNET_DOMAIN
                : BybitApiConfig.TESTNET_DOMAIN;
        return BybitApiClientFactory.newInstance(
                properties.apiKey(), properties.apiSecret(), domain, true);
    }

    @Override
    public Mono<Boolean> cancelAll(Symbol symbol) {
        return null;
    }

    @Override
    public Exchange exchange() {
        return null;
    }
}
