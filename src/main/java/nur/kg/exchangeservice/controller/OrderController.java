package nur.kg.exchangeservice.controller;

import com.bybit.api.client.domain.trade.response.OrderResponse;
import lombok.RequiredArgsConstructor;
import nur.kg.domain.request.OrderRequest;
import nur.kg.exchangeservice.service.OrderService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public Mono<OrderResponse> placeOrder(@RequestBody Mono<OrderRequest> request) {
        return request.flatMap(orderService::placeOrder);
    }
}
