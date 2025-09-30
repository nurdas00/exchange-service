package nur.kg.exchangeservice.client;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import nur.kg.domain.dto.TickerDto;
import nur.kg.exchangeservice.config.AppProperties;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

@Log4j2
@Component
@RequiredArgsConstructor
public class BotClient {

    private WebClient webClient;
    private final AppProperties config;

    @PostConstruct
    public void init() {
        webClient = WebClient.builder().baseUrl(config.getBotUrl()).build();
    }

    public Mono<Void> sendData(Flux<TickerDto> stream) {

        return webClient.post()
                .uri("/api/tickers/stream")
                .contentType(MediaType.APPLICATION_NDJSON)
                .body(stream, TickerDto.class)
                .retrieve()
                .bodyToMono(Void.class)
                .retryWhen(Retry.backoff(Long.MAX_VALUE, Duration.ofSeconds(1))
                        .maxBackoff(Duration.ofSeconds(30)));
    }
}
