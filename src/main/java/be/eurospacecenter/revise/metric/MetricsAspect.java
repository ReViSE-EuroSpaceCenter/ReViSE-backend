package be.eurospacecenter.revise.metric;

import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class MetricsAspect {

    private final AppMetrics metrics;

    public MetricsAspect(AppMetrics metrics) {
        this.metrics = metrics;
    }

    @AfterReturning(pointcut = "@annotation(recordMetric)", returning = "result")
    public void recordMetric(RecordMetric recordMetric, Object result) {
        switch (recordMetric.value()) {
            case GAME_CREATED -> metrics.gameCreated();
            case GAME_JOINED -> metrics.gameJoined();
            case GAME_STARTED -> metrics.gameStarted();
            case GAME_ENDED -> metrics.gameEnded();
            case GAME_CLEARED ->
                    metrics.gameCleared(result instanceof Integer count ? count : 0);
        }
    }
}