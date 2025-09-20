package nur.kg.exchangeservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

public class AsyncConfig {
    @Bean(name = "marketExecutor")
    public Executor marketExecutor() {
        ThreadPoolTaskExecutor exec = new ThreadPoolTaskExecutor();
        exec.setCorePoolSize(8);
        exec.setMaxPoolSize(16);
        exec.setQueueCapacity(200);
        exec.setThreadNamePrefix("market-");
        exec.initialize();
        return exec;
    }
}
