package nur.kg.exchangeservice.domain;

import java.math.BigDecimal;
import java.time.Instant;

public record Ticker(Symbol symbol, BigDecimal last, Instant ts) {

}
