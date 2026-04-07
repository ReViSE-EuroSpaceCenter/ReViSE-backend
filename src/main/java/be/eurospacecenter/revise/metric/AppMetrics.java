package be.eurospacecenter.revise.metric;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class AppMetrics {
    private final Counter gameCreatedCounter;
    private final Counter gameJoinedCounter;
    private final Counter gameStartedCounter;
    private final Counter gameEndedCounter;
    private final Counter gameClearedCounter;

    public AppMetrics(MeterRegistry registry) {
        this.gameCreatedCounter = Counter.builder(MetricType.GAME_CREATED.metricKey).register(registry);
        this.gameJoinedCounter = Counter.builder(MetricType.GAME_JOINED.metricKey).register(registry);
        this.gameStartedCounter = Counter.builder(MetricType.GAME_STARTED.metricKey).register(registry);
        this.gameEndedCounter = Counter.builder(MetricType.GAME_ENDED.metricKey).register(registry);
        this.gameClearedCounter = Counter.builder(MetricType.GAME_CLEARED.metricKey).register(registry);
    }

    public void gameCreated() {
        gameCreatedCounter.increment();
    }

    public void gameJoined() {
        gameJoinedCounter.increment();
    }

    public void gameStarted() {
        gameStartedCounter.increment();
    }

    public void gameEnded() {
        gameEndedCounter.increment();
    }

    public void gameCleared(int count) {
        gameClearedCounter.increment(count);
    }
}
