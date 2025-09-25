package nur.kg.exchangeservice.domain.dto;

import nur.kg.exchangeservice.domain.enums.Exchange;
import nur.kg.exchangeservice.domain.enums.Symbol;

import java.math.BigDecimal;
import java.time.Instant;

public record TickerDto(
        Exchange exchange,
        Symbol symbol,
        BigDecimal last,
        Instant ts
) {}