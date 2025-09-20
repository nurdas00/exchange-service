package nur.kg.exchangeservice.scheduler;

import com.bybit.api.client.domain.market.response.tickers.TickersResult;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import nur.kg.exchangeservice.client.BotClient;
import nur.kg.exchangeservice.enums.Exchange;
import nur.kg.exchangeservice.enums.MarketSymbol;
import nur.kg.exchangeservice.market.MarketSupplier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Log4j2
@Component
@RequiredArgsConstructor
public class MarketSupplyScheduler {

    private final List<MarketSupplier> supplierList;
    private final BotClient client;
    private final Executor marketExecutor;
    private final Map<Exchange, MarketSupplier> supplierMap = new HashMap<>();

    @PostConstruct
    public void init() {
        supplierList.forEach(supplier -> supplierMap.put(supplier.getExchange(), supplier));
    }

    @Scheduled(cron = "0 */5 * * * MON-FRI", zone = "Asia/Bishkek")
    public void run() {
        for (Exchange exchange : Exchange.values()) {
            MarketSupplier marketSupplier = supplierMap.get(exchange);
            for (MarketSymbol symbol : MarketSymbol.values()) {
                CompletableFuture.runAsync(() -> {
                    try {
                        TickersResult result = marketSupplier.getTickers(symbol);
                        String lastPrice = result.getTickerEntries().get(0).getLastPrice();
                        client.sendData(symbol, Float.parseFloat(lastPrice));
                    } catch (Exception e) {
                        log.warn("Failed for {} {}: {}", exchange, symbol, e.toString());
                    }
                }, marketExecutor);
            }
        }
    }
}
