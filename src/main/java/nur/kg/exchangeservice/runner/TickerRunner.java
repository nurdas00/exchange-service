package nur.kg.exchangeservice.runner;


import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import nur.kg.domain.dto.TickerDto;
import nur.kg.domain.enums.Exchange;
import nur.kg.domain.enums.Symbol;
import nur.kg.domain.model.Ticker;
import nur.kg.exchangeservice.client.BotClient;
import nur.kg.exchangeservice.market.data.MarketDataClient;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;
import reactor.core.Disposable;
import reactor.core.Disposables;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@Log4j2
@Component
@RequiredArgsConstructor
public class TickerRunner implements SmartLifecycle {

    private final List<MarketDataClient> sources;
    private final BotClient bot;
    private Disposable.Composite bag;

    @Override
    public synchronized void start() {
        if (isRunning()) {
            log.info("TickerRunner already running, skip start()");
            return;
        }
        bag = Disposables.composite();
        Set<Symbol> symbols = Set.of(Symbol.values());
        Flux<TickerDto> dtoStream = Flux.merge(
                        sources.stream().map(src ->
                                src.streamTickers(symbols)
                                        .map(t -> toDto(src.exchange(), t))
                        ).toList()
                )
                .onBackpressureLatest()
                .sample(Duration.ofMillis(100));

        Disposable d = bot.sendData(dtoStream).subscribe();
        bag.add(d);
        log.info("TickerRunner started: {} sources", sources.size());
    }

    @Override
    public synchronized void stop() {
        if (bag != null) {
            bag.dispose();
            bag = null;
            log.info("TickerRunner stopped");
        }
    }

    @Override
    public boolean isRunning() {
        return bag != null && !bag.isDisposed();
    }

    private static TickerDto toDto(Exchange exchange, Ticker t) {
        return new TickerDto(exchange, t.symbol(), t.last(), t.ts());
    }

}
