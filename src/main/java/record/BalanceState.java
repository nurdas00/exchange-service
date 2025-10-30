package record;

import java.math.BigDecimal;

public record BalanceState(BigDecimal initial, BigDecimal current) {}