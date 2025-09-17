package nur.kg.exchangeservice.data;

import com.bybit.api.client.config.BybitApiConfig;
import com.bybit.api.client.domain.CategoryType;
import com.bybit.api.client.domain.market.MarketInterval;
import com.bybit.api.client.domain.market.request.MarketDataRequest;
import com.bybit.api.client.restApi.BybitApiMarketRestClient;
import com.bybit.api.client.service.BybitApiClientFactory;
import lombok.SneakyThrows;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ExchangeSupplier {

    @SneakyThrows
    public void test() {
        BybitApiMarketRestClient bybitApiMarketRestClient =
                BybitApiClientFactory.newInstance(BybitApiConfig.TESTNET_DOMAIN, false)
                        .newMarketDataRestClient();
        MarketDataRequest marketDataRequest = MarketDataRequest.builder()
                .category(CategoryType.LINEAR)
                .symbol("BTCUSDT")
                .marketInterval(MarketInterval.ONE_MINUTE)
                .build();

        Map<String, Object> obj = (LinkedHashMap) bybitApiMarketRestClient.getMarketLinesData(marketDataRequest);

        Map<String, Object> result = (Map<String, Object>) obj.get("result");

        List<String> list = (List<String>) result.get("list");

        System.out.println(list);
    }
}
