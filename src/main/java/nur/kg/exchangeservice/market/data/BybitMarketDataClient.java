package nur.kg.exchangeservice.market.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import nur.kg.domain.enums.Exchange;
import nur.kg.domain.enums.Symbol;
import nur.kg.domain.model.Ticker;
import nur.kg.exchangeservice.config.BybitProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.math.BigDecimal;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.Set;

@Log4j2
@Component
@RequiredArgsConstructor
public class BybitMarketDataClient implements MarketDataClient {

    private URI webSocketUri;
    private final ObjectMapper mapper;
    private final BybitProperties properties;
    private final ReactorNettyWebSocketClient ws = new ReactorNettyWebSocketClient();

    @PostConstruct
    public void init() {
        webSocketUri = URI.create(properties.wsUrl());
    }

    @Override
    public Flux<Ticker> streamTickers(Set<Symbol> symbols) {
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
                                if (t != null) {
                                    log.info("Received ticker: {}}", text);
                                    sink.next(t);
                                }
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
    public Exchange exchange() {
        return Exchange.BYBIT;
    }
}
