package nur.kg.exchangeservice.service;

import com.bybit.api.client.domain.trade.response.OrderResponse;
import lombok.RequiredArgsConstructor;
import nur.kg.domain.request.OrderRequest;
import nur.kg.exchangeservice.market.registry.ExchangeRegistry;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final ExchangeRegistry registry;


    public Mono<OrderResponse> placeOrder(OrderRequest request) {
        return registry.trading(request.exchange()).placeOrder(request);
    }
}
