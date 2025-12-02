package nur.kg.exchangeservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "analytics")
public record AnalyticsProperties(String url) {}
