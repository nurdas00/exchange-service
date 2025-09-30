package nur.kg.exchangeservice.market;

import com.bybit.api.client.domain.trade.response.OrderResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import nur.kg.domain.enums.Exchange;
import nur.kg.domain.enums.Symbol;
import nur.kg.domain.model.Ticker;
import nur.kg.domain.request.OrderRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import org.springframework.web.reactive.socket.client.WebSocketClient;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.math.BigDecimal;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Component
public class BybitClient implements ExchangeClient {

    @Value("${bybit.url}")
    private String bybitWsUri;
    private URI webSocketUri;
    private final WebClient client = WebClient.builder().baseUrl(bybitWsUri).build();
    private final WebSocketClient ws = new ReactorNettyWebSocketClient();
    private final ObjectMapper mapper = new ObjectMapper();

    @PostConstruct
    public void init() {
        webSocketUri = URI.create(bybitWsUri);
    }
    @Override
    public Flux<Ticker> streamTickers() {
        List<Symbol> symbols = List.of(Symbol.values());
        final String subscribeMsg = """
                {"op":"subscribe","args":%s}
                """.formatted(
                mapper.valueToTree(
                        symbols.stream().map(i -> "tickers." + i).toList()
                ).toString()
        );

        return Flux.<Ticker>create(sink -> {
            Mono<Void> sessionMono = ws.execute(webSocketUri, wsSession -> {
                Flux<WebSocketMessage> outbound = Flux.concat(
                        Mono.fromSupplier(() -> wsSession.textMessage(subscribeMsg)),
                        Flux.interval(Duration.ofSeconds(20)).map(i -> wsSession.textMessage("{\"op\":\"ping\"}"))
                );

                Mono<Void> send = wsSession.send(outbound).then();

                Mono<Void> receive = wsSession.receive()
                        .map(WebSocketMessage::getPayloadAsText)
                        .doOnNext(text -> {
                            try {
                                JsonNode n = mapper.readTree(text);
                                Ticker t = parseTicker(n);
                                if (t != null) sink.next(t);
                            } catch (Exception ignore) {
                            }
                        })
                        .then();

                return Mono.when(send, receive);
            });

            Disposable d = sessionMono.subscribe(null, sink::error, sink::complete);
            sink.onDispose(d);
        }).retryWhen(
                Retry.backoff(Long.MAX_VALUE, Duration.ofSeconds(1)).maxBackoff(Duration.ofSeconds(30))
        ).name("bybit-tickers-webflux");
    }

    private Ticker parseTicker(JsonNode frame) {
        if (frame == null || !frame.hasNonNull("topic")) {
            return null;
        }
        String topic = frame.get("topic").asText();
        if (!topic.startsWith("tickers.")) {
            return null;
        }

        JsonNode data = frame.get("data");
        if (data == null) return null;

        String instrument = data.path("symbol").asText(null);
        if (instrument == null) return null;

        Symbol sym = Symbol.valueOf(instrument);
        BigDecimal last = asBD(data.path("lastPrice").asText(null));
        long tsMs = data.has("ts") ? data.get("ts").asLong() : System.currentTimeMillis();

        return new Ticker(sym, last, Instant.ofEpochMilli(tsMs));
    }

    private static BigDecimal asBD(String s) {
        try {
            return (s == null) ? null : new BigDecimal(s);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public Mono<OrderResponse> placeOrder(OrderRequest request) {
        return client.post()
                .uri("/v5/order/create") // example
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(OrderResponse.class);
    }

    @Override
    public Mono<Boolean> cancelAll(Symbol symbol) {
        return null;
    }

    @Override
    public Exchange getExchange() {
        return Exchange.BYBIT;
    }
}
