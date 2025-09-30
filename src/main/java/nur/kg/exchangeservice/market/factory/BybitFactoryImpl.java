package nur.kg.exchangeservice.market.factory;

import lombok.RequiredArgsConstructor;
import nur.kg.domain.enums.Exchange;
import nur.kg.exchangeservice.market.data.BybitMarketDataClient;
import nur.kg.exchangeservice.market.data.MarketDataClient;
import nur.kg.exchangeservice.market.trading.BybitTradingClient;
import nur.kg.exchangeservice.market.trading.TradingClient;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BybitFactoryImpl implements ExchangeFactory {

    private final BybitMarketDataClient marketDataClient;
    private final BybitTradingClient tradingClient;
    @Override
    public MarketDataClient marketData() {
        return marketDataClient;
    }

    @Override
    public TradingClient trading() {
        return tradingClient;
    }

    @Override
    public Exchange exchange() {
        return Exchange.BYBIT;
    }
}
