package nur.kg.exchangeservice.domain.model;

import nur.kg.exchangeservice.domain.enums.Symbol;

import java.math.BigDecimal;
import java.time.Instant;

public record Ticker(Symbol symbol, BigDecimal last, Instant ts) {

}
