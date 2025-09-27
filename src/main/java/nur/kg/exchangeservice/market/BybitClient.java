package nur.kg.exchangeservice.market;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import nur.kg.domain.enums.Exchange;
import nur.kg.domain.enums.Symbol;
import nur.kg.domain.model.Ticker;
import nur.kg.domain.request.OrderRequest;
import nur.kg.domain.response.OrderResponse;
import org.springframework.stereotype.Component;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.client.WebsocketClientSpec;
import reactor.util.retry.Retry;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Component
public class BybitClient implements ExchangeClient {

    private final String bybitWsUri = "wss://stream-testnet.bybit.com/v5/public/spot";
    private final ObjectMapper mapper = new ObjectMapper();

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
            Mono<Void> session = HttpClient.create()
                    .websocket(WebsocketClientSpec.builder().handlePing(true).build())
                    .uri(bybitWsUri)
                    .handle((in, out) -> {
                        Flux<String> inbound = in.receive().asString();

                        Mono<Void> process = inbound
                                .doOnNext(text -> {
                                    try {
                                        JsonNode n = mapper.readTree(text);
                                        Ticker t = tryParseTicker(n);
                                        if (t != null ) {
                                            sink.next(t);
                                        }
                                    } catch (Exception ignore) { }
                                })
                                .doOnError(sink::error)
                                .then();

                        Mono<Void> send = out.sendString(Flux.concat(
                                Mono.just(subscribeMsg),
                                Flux.interval(Duration.ofSeconds(20)).map(i -> "{\"op\":\"ping\"}")
                        )).then();

                        return Mono.when(send, process);

                    }).then();

            Disposable disposable = session
                    .doOnError(sink::error)
                    .doFinally(st -> sink.complete())
                    .subscribe();

            sink.onDispose(disposable);
        }).retryWhen(
                Retry.backoff(Long.MAX_VALUE, Duration.ofSeconds(1)).maxBackoff(Duration.ofSeconds(30))
        ).name("bybit-tickers");
    }

    private Ticker tryParseTicker(JsonNode frame) {
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
    public Mono<OrderResponse> placeOrder(OrderRequest intent) {
        return null;
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
