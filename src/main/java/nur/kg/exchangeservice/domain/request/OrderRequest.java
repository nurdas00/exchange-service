package nur.kg.exchangeservice.domain.request;

import nur.kg.exchangeservice.domain.enums.OrderType;
import nur.kg.exchangeservice.domain.enums.Side;
import nur.kg.exchangeservice.domain.enums.Symbol;

import java.math.BigDecimal;

public record OrderRequest(String id, Symbol symbol, Side side, OrderType type,
                           BigDecimal qty, BigDecimal limitPrice, String reason) {}