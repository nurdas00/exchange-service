package nur.kg.exchangeservice.market;

import com.bybit.api.client.domain.trade.response.OrderResponse;
import nur.kg.domain.request.OrderRequest;
import nur.kg.domain.enums.Symbol;
import nur.kg.domain.model.Ticker;
import nur.kg.domain.enums.Exchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ExchangeClient {

    Exchange getExchange();
    Flux<Ticker> streamTickers();
    Mono<OrderResponse> placeOrder(OrderRequest request);
    Mono<Boolean> cancelAll(Symbol symbol);
}
