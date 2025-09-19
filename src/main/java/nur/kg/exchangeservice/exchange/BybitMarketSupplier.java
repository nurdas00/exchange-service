package nur.kg.exchangeservice.exchange;

import com.bybit.api.client.config.BybitApiConfig;
import com.bybit.api.client.domain.CategoryType;
import com.bybit.api.client.domain.market.request.MarketDataRequest;
import com.bybit.api.client.domain.market.response.tickers.TickersResult;
import com.bybit.api.client.restApi.BybitApiMarketRestClient;
import com.bybit.api.client.service.BybitApiClientFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import nur.kg.exchangeservice.enums.MarketSymbol;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class BybitMarketSupplier implements MarketSupplier {

    @SneakyThrows
    public TickersResult getTickers(MarketSymbol symbol) {
        BybitApiMarketRestClient bybitApiMarketRestClient =
                BybitApiClientFactory.newInstance(BybitApiConfig.TESTNET_DOMAIN, false)
                        .newMarketDataRestClient();

        MarketDataRequest marketDataRequest = MarketDataRequest.builder()
                .category(CategoryType.SPOT)
                .symbol(symbol.name())
                .build();
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> response = (Map<String, Object>) bybitApiMarketRestClient.getMarketTickers(marketDataRequest);
        return mapper.readValue(mapper.writeValueAsString(response.get("result")), TickersResult.class);
    }
}