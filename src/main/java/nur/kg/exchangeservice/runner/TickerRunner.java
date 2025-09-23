package nur.kg.exchangeservice.runner;


import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import nur.kg.exchangeservice.domain.Ticker;
import nur.kg.exchangeservice.market.ExchangeClient;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;
import reactor.core.Disposable;
import reactor.core.Disposables;

import java.util.List;

@Log4j2
@Component
@RequiredArgsConstructor
public class TickerRunner implements SmartLifecycle {

    private final List<ExchangeClient> sources;
    private Disposable.Composite bag;

    @Override
    public synchronized void start() {
        if (isRunning()) {
            log.info("TickerRunner already running, skip start()");
            return;
        }
        bag = Disposables.composite();

        for (ExchangeClient s : sources) {
            Disposable d = s.streamTickers()
                    .doOnSubscribe(sub -> log.info("Start market-data stream: {}", s.getExchange().name()))
                    .doOnError(e -> log.error("Stream error ({}): {}", s.getExchange().name(), e.toString()))
                    .subscribe(
                            t -> onTick(s.getExchange().name(), t),
                            e -> onError(s.getExchange().name(), e)
                    );

            bag.add(d);
        }

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

    protected void onTick(String sourceName, Ticker t) {
        //TODO: send to bot
        log.debug("[{}] {}", sourceName, t);
    }

    protected void onError(String sourceName, Throwable e) {
        log.error("Market-data stream failed for {}: {}", sourceName, e.getMessage(), e);
    }
}
