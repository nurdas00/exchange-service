package nur.kg.exchangeservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "bot")
public record BotProperties(String url) {
}