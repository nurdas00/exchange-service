package nur.kg.exchangeservice.market.data;

import nur.kg.domain.enums.Exchange;
import nur.kg.domain.enums.Symbol;
import nur.kg.domain.model.Ticker;
import reactor.core.publisher.Flux;

import java.util.Set;

public interface MarketDataClient {
    Flux<Ticker> streamTickers(Set<Symbol> symbols);
    Exchange exchange();
}