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
        if (recordMetric.value() == MetricType.LOBBY_CLEARED && result instanceof Integer count) {
            metrics.lobbyCleared(count);
        }

        switch (recordMetric.value()) {
            case LOBBY_CREATED -> metrics.lobbyCreated();
            case LOBBY_JOINED -> metrics.lobbyJoined();
            case LOBBY_STARTED -> metrics.lobbyStarted();
            default -> throw new IllegalStateException("Unknown metric : " + recordMetric);
        }
    }
}