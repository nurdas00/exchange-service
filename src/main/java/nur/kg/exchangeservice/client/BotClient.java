package nur.kg.exchangeservice.client;

import nur.kg.exchangeservice.domain.Ticker;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class BotClient {

    private final WebClient webClient = WebClient.builder().build();
    public void sendData(Ticker ticker) {

        webClient.post();
    }
}
