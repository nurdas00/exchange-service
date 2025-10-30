package nur.kg.exchangeservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import nur.kg.domain.enums.Symbol;
import nur.kg.exchangeservice.market.trading.BybitTradingClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import record.BalanceState;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Log4j2
@Service
@RequiredArgsConstructor
public class BotsPortfolioService {
    private final BybitTradingClient client;

    private final Map<String, Map<Symbol, BalanceState>> botMarketBalances = new ConcurrentHashMap<>();

    public Mono<Void> registerBot(String botId, Symbol symbol) {
        return client.getBalance()
                .doOnNext(balance -> {
                    botMarketBalances
                            .computeIfAbsent(botId, k -> new ConcurrentHashMap<>())
                            .put(symbol, new BalanceState(balance, balance));
                    log.info("Registered bot {} for market {} with initial balance {}", botId, symbol, balance);
                })
                .then();
    }

    public Mono<Void> updateAfterTrade(String botId, Symbol symbol, String reason) {
        Map<Symbol, BalanceState> marketMap = botMarketBalances.get(botId);
        if (marketMap == null || !marketMap.containsKey(symbol)) {
            return registerBot(botId, symbol)
                    .then(updateAfterTrade(botId, symbol, reason));
        }

        BalanceState state = marketMap.get(symbol);

        return client.getBalance()
                .doOnNext(bal -> {
                    BigDecimal prev = state.current();
                    BigDecimal delta = bal.subtract(prev);
                    BigDecimal totalPnL = bal.subtract(state.initial());
                    marketMap.put(symbol, new BalanceState(state.initial(), bal));

                    log.info("Bot {} | Market {} | {} | Δ: {} | Total PnL: {}",
                            botId, symbol, reason, delta, totalPnL);
                })
                .then();
    }

    public BigDecimal getPnL(String botId, Symbol symbol) {
        Map<Symbol, BalanceState> marketMap = botMarketBalances.get(botId);
        if (marketMap == null) return BigDecimal.ZERO;
        BalanceState s = marketMap.get(symbol);
        if (s == null) return BigDecimal.ZERO;
        return s.current().subtract(s.initial());
    }

    public BigDecimal getTotalPnL(String botId) {
        Map<Symbol, BalanceState> marketMap = botMarketBalances.get(botId);
        if (marketMap == null) return BigDecimal.ZERO;
        return marketMap.values().stream()
                .map(s -> s.current().subtract(s.initial()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
