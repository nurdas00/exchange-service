package nur.kg.exchangeservice.service;

import com.bybit.api.client.domain.trade.response.OrderResponse;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import nur.kg.domain.enums.Exchange;
import nur.kg.domain.request.OrderRequest;
import nur.kg.exchangeservice.market.ExchangeClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final List<ExchangeClient> clientsList;
    private final Map<Exchange, ExchangeClient> clientMap;

    @PostConstruct
    public void init() {
        clientsList.forEach(c -> clientMap.put(c.getExchange(), c));
    }

    public Mono<OrderResponse> placeOrder(OrderRequest request) {
        return clientMap.get(request.exchange()).placeOrder(request);
    }
}
