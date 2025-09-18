package nur.kg.exchangeservice;

import com.fasterxml.jackson.core.JsonProcessingException;
import nur.kg.exchangeservice.data.ExchangeSupplier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ExchangeServiceApplication {

    public static void main(String[] args) throws JsonProcessingException {
        ExchangeSupplier ex = new ExchangeSupplier();
        ex.test();
        SpringApplication.run(ExchangeServiceApplication.class, args);
    }

}
