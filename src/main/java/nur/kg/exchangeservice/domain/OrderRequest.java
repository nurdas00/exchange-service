package nur.kg.exchangeservice.domain;

import java.math.BigDecimal;

public record OrderRequest(String id, Symbol symbol, Side side, OrderType type,
                           BigDecimal qty, BigDecimal limitPrice, String reason) {}