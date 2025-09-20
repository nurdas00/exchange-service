package nur.kg.exchangeservice;

import com.fasterxml.jackson.core.JsonProcessingException;
import nur.kg.exchangeservice.market.BybitMarketSupplier;
import nur.kg.exchangeservice.enums.MarketSymbol;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class ExchangeServiceApplication {

    public static void main(String[] args) throws JsonProcessingException {
        BybitMarketSupplier ex = new BybitMarketSupplier();
        ex.getTickers(MarketSymbol.BTCUSDT);
        SpringApplication.run(ExchangeServiceApplication.class, args);
    }

}
