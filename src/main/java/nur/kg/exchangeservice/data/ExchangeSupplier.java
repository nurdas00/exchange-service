package nur.kg.exchangeservice.data;

import com.bybit.api.client.config.BybitApiConfig;
import com.bybit.api.client.domain.CategoryType;
import com.bybit.api.client.domain.market.request.MarketDataRequest;
import com.bybit.api.client.domain.market.response.tickers.TickersResult;
import com.bybit.api.client.restApi.BybitApiMarketRestClient;
import com.bybit.api.client.service.BybitApiClientFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

public class ExchangeSupplier {

    public void test() throws JsonProcessingException {
        BybitApiMarketRestClient bybitApiMarketRestClient =
                BybitApiClientFactory.newInstance(BybitApiConfig.TESTNET_DOMAIN, false)
                        .newMarketDataRestClient();

        MarketDataRequest marketDataRequest = MarketDataRequest.builder()
                .category(CategoryType.SPOT)
                .symbol("BTCUSDT")
                .build();
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> response = (Map<String, Object>) bybitApiMarketRestClient.getMarketTickers(marketDataRequest);
        TickersResult result = mapper.readValue(mapper.writeValueAsString(response.get("result")), TickersResult.class);
        System.out.println(result);

    }
}
