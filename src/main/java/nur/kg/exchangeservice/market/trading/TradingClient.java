package nur.kg.exchangeservice.market.trading;

import com.bybit.api.client.domain.trade.response.OrderResponse;
import nur.kg.domain.enums.Exchange;
import nur.kg.domain.enums.Symbol;
import nur.kg.domain.request.OrderRequest;
import reactor.core.publisher.Mono;

public interface TradingClient {
    Mono<OrderResponse> placeOrder(OrderRequest request);
    Mono<Boolean> cancelAll(Symbol symbol);
    Exchange exchange();
}
