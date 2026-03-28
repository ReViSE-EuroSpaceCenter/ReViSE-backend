package be.eurospacecenter.revise.metric;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AppMetricsTest {

    private AppMetrics appMetrics;
    private MeterRegistry meterRegistry;

    // ---------------------------------------------------------------------
    // Setup
    // ---------------------------------------------------------------------

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        appMetrics = new AppMetrics(meterRegistry);
    }

    // ---------------------------------------------------------------------
    // Tests
    // ---------------------------------------------------------------------

    @ParameterizedTest
    @ValueSource(ints = {1, 5, 0, 10})
    void testLobbyClearedIncrementsCounter(int count) {
        appMetrics.lobbyCleared(count);

        var counter = meterRegistry.find(MetricType.LOBBY_CLEARED.metricKey).counter();
        assertNotNull(counter, "Counter should be registered");
        assertEquals(count, counter.count());
    }

    @Test
    void testLobbyClearedMultipleCalls() {
        int count1 = 2;
        int count2 = 3;

        appMetrics.lobbyCleared(count1);
        appMetrics.lobbyCleared(count2);

        var counter = meterRegistry.find(MetricType.LOBBY_CLEARED.metricKey).counter();
        assertNotNull(counter, "Counter should be registered");
        assertEquals(5.0, counter.count());
    }
}


