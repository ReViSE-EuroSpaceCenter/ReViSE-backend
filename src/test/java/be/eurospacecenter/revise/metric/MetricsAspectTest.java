package be.eurospacecenter.revise.metric;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class MetricsAspectTest {

    private MeterRegistry meterRegistry;
    private TestService testService;

    // ---------------------------------------------------------------------
    // Setup
    // ---------------------------------------------------------------------

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        var appMetrics = new AppMetrics(meterRegistry);
        var metricsAspect = new MetricsAspect(appMetrics);

        AspectJProxyFactory factory = new AspectJProxyFactory(new TestServiceImpl());
        factory.addAspect(metricsAspect);
        testService = factory.getProxy();
    }

    // ---------------------------------------------------------------------
    // Tests
    // ---------------------------------------------------------------------

    @Test
    void testComprehensiveMetricScenario() {
        testService.createLobby();
        testService.joinLobby();
        testService.joinLobby();
        testService.startGame();

        int count = testService.clearLobbies() + testService.clearLobbiesWithCount(2);

        assertMetric(MetricType.GAME_CREATED, 1.0);
        assertMetric(MetricType.GAME_JOINED, 2.0);
        assertMetric(MetricType.GAME_STARTED, 1.0);
        assertMetric(MetricType.GAME_CLEARED, count);
    }

    // ---------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------

    private void assertMetric(MetricType type, double expected) {
        var counter = meterRegistry.find(type.metricKey).counter();
        assertNotNull(counter, "Missing counter: " + type.name());
        assertEquals(expected, counter.count());
    }

    interface TestService {
        void createLobby();

        void joinLobby();

        void startGame();

        int clearLobbies();

        int clearLobbiesWithCount(int count);
    }

    static class TestServiceImpl implements TestService {
        @RecordMetric(MetricType.GAME_CREATED)
        public void createLobby() {
            // Placeholder for lobby creation logic
        }

        @RecordMetric(MetricType.GAME_JOINED)
        public void joinLobby() {
            // Placeholder for lobby joining logic
        }

        @RecordMetric(MetricType.GAME_STARTED)
        public void startGame() {
            // Placeholder for lobby starting logic
        }

        @RecordMetric(MetricType.GAME_CLEARED)
        public int clearLobbies() {
            return 1;
        }

        @RecordMetric(MetricType.GAME_CLEARED)
        public int clearLobbiesWithCount(int count) {
            return count;
        }
    }
}