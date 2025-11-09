package nur.kg.exchangeservice.runner;

import com.bybit.api.client.config.BybitApiConfig;
import com.bybit.api.client.domain.CategoryType;
import com.bybit.api.client.domain.trade.request.TradeOrderRequest;
import com.bybit.api.client.service.BybitApiClientFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import nur.kg.exchangeservice.config.BybitProperties;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@RequiredArgsConstructor
public class StartupOpenOrdersLogger {

    private final BybitProperties props;
    private final ObjectMapper mapper;
    private static final int RECV_WINDOW = 10000;

    @PostConstruct
    public void logAllOpenOrders() {
        TradeOrderRequest.TradeOrderRequestBuilder openOrderRequest = TradeOrderRequest.builder();
        var client = BybitApiClientFactory.newInstance(props.apiKey(), props.apiSecret(), BybitApiConfig.TESTNET_DOMAIN).newTradeRestClient();
        Object raw = client.getOpenOrders(openOrderRequest.category(CategoryType.LINEAR).openOnly(0).symbol("BTCUSDT").build());
        ObjectMapper mapper = new ObjectMapper();
        BybitOrdersResponse response = mapper.convertValue(raw, BybitOrdersResponse.class);

        if (response.getResult() != null && response.getResult().getList() != null) {
            System.out.println("Total orders: " + response.getResult().getList().size());
            response.getResult().getList().forEach(o ->
                    System.out.printf("%s %s %s qty=%s price=%s status=%s%n",
                            o.getSymbol(), o.getSide(), o.getOrderType(),
                            o.getQty(), o.getPrice(), o.getOrderStatus())
            );
        } else {
            System.out.println("No orders found or result was null");
        }
    }
}
