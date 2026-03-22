package be.eurospacecenter.revise.config;

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
        this.lobbyClearedCounter = Counter.builder("lobby_cleared_counter").register(registry);
        this.lobbyCreatedCounter = Counter.builder("lobby_created_counter").register(registry);
        this.lobbyJoinedCounter = Counter.builder("lobby_joined_counter").register(registry);
        this.lobbyStartedCounter = Counter.builder("lobby_started_counter").register(registry);
    }

    public void lobbyCleared() {
        lobbyClearedCounter.increment();
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
