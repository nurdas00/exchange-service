package nur.kg.exchangeservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "bot")
public record BotProperties(List<BotEndpoint> endpoints) {}