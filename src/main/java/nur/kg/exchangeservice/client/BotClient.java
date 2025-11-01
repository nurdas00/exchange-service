package nur.kg.exchangeservice.client;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import nur.kg.domain.dto.TickerDto;
import nur.kg.exchangeservice.config.BotEndpoint;
import nur.kg.exchangeservice.config.BotProperties;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Log4j2
@Component
@RequiredArgsConstructor
public class BotClient {

    private final BotProperties config;
    private final Map<String, WebClient> clients = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        for (BotEndpoint ep : config.endpoints()) {
            clients.put(ep.name(),
                    WebClient.builder()
                            .baseUrl(ep.url())
                            .build());
        }
        log.info("Loaded {} bot endpoints: {}", clients.size(), clients.keySet());
    }

    public Mono<Void> sendDataToAll(Flux<TickerDto> stream) {
        if (clients.isEmpty()) return Mono.empty();

        Flux<TickerDto> shared = stream.publish().autoConnect(clients.size());
        return Mono.whenDelayError(
                clients.entrySet().stream()
                        .map(e -> sendTo(e.getKey(), e.getValue(), shared))
                        .toList());
    }

    private Mono<Void> sendTo(String name, WebClient wc, Flux<TickerDto> stream) {
        return wc.post()
                .uri("/api/tickers/stream")
                .contentType(MediaType.APPLICATION_NDJSON)
                .body(stream, TickerDto.class)
                .retrieve()
                .bodyToMono(Void.class)
                .doOnSubscribe(s -> log.info("Started sending to {}", name))
                .doOnError(e -> log.warn("Error sending to {}: {}", name, e.toString()))
                .retryWhen(Retry.backoff(Long.MAX_VALUE, Duration.ofSeconds(1))
                        .maxBackoff(Duration.ofSeconds(30)));
    }
}