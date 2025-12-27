package nur.kg.exchangeservice.controller;

import com.bybit.api.client.domain.trade.response.OrderResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import nur.kg.domain.request.OrderRequest;
import nur.kg.exchangeservice.service.OrderService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Log4j2
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public Mono<OrderResponse> placeOrder(@RequestBody Mono<OrderRequest> request) {
        log.info("Receive request: {}",  request);

        return request.flatMap(orderService::placeOrder);
    }
}