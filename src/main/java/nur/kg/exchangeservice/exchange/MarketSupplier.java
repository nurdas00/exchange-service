package nur.kg.exchangeservice.exchange;

import com.bybit.api.client.domain.market.response.tickers.TickersResult;
import nur.kg.exchangeservice.enums.MarketSymbol;

public interface MarketSupplier {

    TickersResult getTickers(MarketSymbol symbol);
}
