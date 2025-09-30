package nur.kg.exchangeservice.market.registry;

import nur.kg.domain.enums.Exchange;
import nur.kg.exchangeservice.market.data.MarketDataClient;
import nur.kg.exchangeservice.market.factory.ExchangeFactory;
import nur.kg.exchangeservice.market.trading.TradingClient;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ExchangeRegistry {
    private final Map<Exchange, ExchangeFactory> factories;

    public ExchangeRegistry(List<ExchangeFactory> factories) {
        this.factories = factories.stream()
                .collect(Collectors.toUnmodifiableMap(ExchangeFactory::exchange, f -> f));
    }

    public MarketDataClient market(Exchange ex) { return factories.get(ex).marketData(); }
    public TradingClient trading(Exchange ex) { return factories.get(ex).trading(); }
}