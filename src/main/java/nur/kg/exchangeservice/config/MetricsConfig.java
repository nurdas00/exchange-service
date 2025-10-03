package nur.kg.exchangeservice.config;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class MetricsConfig {
    @Bean
    MeterRegistryCustomizer<MeterRegistry> commonTags() {
        return reg -> reg.config().commonTags("service", "exchange-service", "env", "dev");
    }
}