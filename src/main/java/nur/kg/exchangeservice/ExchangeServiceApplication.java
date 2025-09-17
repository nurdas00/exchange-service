package nur.kg.exchangeservice;

import nur.kg.exchangeservice.data.ExchangeSupplier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ExchangeServiceApplication {

	public static void main(String[] args) {
		ExchangeSupplier ex = new ExchangeSupplier();
		ex.test();
		SpringApplication.run(ExchangeServiceApplication.class, args);
	}

}
