package be.eurospacecenter.revise.metric;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class AppMetrics {
    private final Counter lobbyClearedCounter;
    private final Counter lobbyCreatedCounter;
    private final Counter lobbyJoinedCounter;
    private final Counter lobbyStartedCounter;

    public AppMetrics(MeterRegistry registry) {
        this.lobbyClearedCounter = Counter.builder(MetricType.LOBBY_CLEARED.metricKey).register(registry);
        this.lobbyCreatedCounter = Counter.builder(MetricType.LOBBY_CREATED.metricKey).register(registry);
        this.lobbyJoinedCounter = Counter.builder(MetricType.LOBBY_JOINED.metricKey).register(registry);
        this.lobbyStartedCounter = Counter.builder(MetricType.LOBBY_STARTED.metricKey).register(registry);
    }

    public void lobbyCleared(int count) {
        lobbyClearedCounter.increment(count);
    }

    public void lobbyCreated() {
        lobbyCreatedCounter.increment();
    }

    public void lobbyJoined() {
        lobbyJoinedCounter.increment();
    }

    public void lobbyStarted() {
        lobbyStartedCounter.increment();
    }
}
