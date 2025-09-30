package nur.kg.exchangeservice.market.factory;

import nur.kg.domain.enums.Exchange;
import nur.kg.exchangeservice.market.data.MarketDataClient;
import nur.kg.exchangeservice.market.trading.TradingClient;

public interface ExchangeFactory {
    MarketDataClient marketData();
    TradingClient trading();
    Exchange exchange();
}
