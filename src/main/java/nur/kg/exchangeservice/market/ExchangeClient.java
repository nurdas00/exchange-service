package nur.kg.exchangeservice.market;

import nur.kg.exchangeservice.domain.request.OrderRequest;
import nur.kg.exchangeservice.domain.response.OrderResponse;
import nur.kg.exchangeservice.domain.enums.Symbol;
import nur.kg.exchangeservice.domain.model.Ticker;
import nur.kg.exchangeservice.domain.enums.Exchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ExchangeClient {

    Exchange getExchange();
    Flux<Ticker> streamTickers();
    Mono<OrderResponse> placeOrder(OrderRequest intent);
    Mono<Boolean> cancelAll(Symbol symbol);
}
