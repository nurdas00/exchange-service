package nur.kg.exchangeservice.performance;


import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

import java.util.concurrent.TimeUnit;

public final class Perf {
    private Perf() {}

    public static Timer timer(MeterRegistry reg, String name, String... tags) {
        return Timer.builder(name).tags(tags).register(reg);
    }

    public static Counter counter(MeterRegistry reg, String name, String... tags) {
        return Counter.builder(name).tags(tags).register(reg);
    }

    public static void recordNanos(Timer timer, long nanos) {
        timer.record(nanos, TimeUnit.NANOSECONDS);
    }
}