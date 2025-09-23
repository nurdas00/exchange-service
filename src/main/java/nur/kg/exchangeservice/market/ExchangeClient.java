package nur.kg.exchangeservice.market;

import nur.kg.exchangeservice.domain.OrderRequest;
import nur.kg.exchangeservice.domain.OrderResponse;
import nur.kg.exchangeservice.domain.Symbol;
import nur.kg.exchangeservice.domain.Ticker;
import nur.kg.exchangeservice.enums.Exchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ExchangeClient {

    Exchange getExchange();
    Flux<Ticker> streamTickers();
    Mono<OrderResponse> placeOrder(OrderRequest intent);
    Mono<Boolean> cancelAll(Symbol symbol);
}
