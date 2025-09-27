package nur.kg.exchangeservice.client;

import nur.kg.domain.dto.TickerDto;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

@Component
public class BotClient {

    private final WebClient webClient = WebClient.builder().baseUrl("localhost:8081").build();

    public Mono<Void> sendData(Flux<TickerDto> stream) {

        return webClient.post()
                .uri("api/tickers/stream")
                .contentType(MediaType.APPLICATION_NDJSON)
                .body(stream, TickerDto.class)
                .retrieve()
                .bodyToMono(Void.class)
                .retryWhen(Retry.backoff(Long.MAX_VALUE, Duration.ofSeconds(1))
                        .maxBackoff(Duration.ofSeconds(30)));
    }
}
