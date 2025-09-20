package nur.kg.exchangeservice.market;

import com.bybit.api.client.domain.market.response.tickers.TickersResult;
import nur.kg.exchangeservice.enums.Exchange;
import nur.kg.exchangeservice.enums.MarketSymbol;

public interface MarketSupplier {

    TickersResult getTickers(MarketSymbol symbol);

    Exchange getExchange();
}
