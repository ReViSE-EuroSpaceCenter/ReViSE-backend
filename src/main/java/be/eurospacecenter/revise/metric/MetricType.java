package be.eurospacecenter.revise.metric;

public enum MetricType {
    GAME_CREATED("game_created_counter"),
    GAME_JOINED("game_joined_counter"),
    GAME_STARTED("game_started_counter"),
    GAME_ENDED("game_ended_counter"),
    GAME_CLEARED("game_cleared_counter");

    public final String metricKey;

    MetricType(String metricKey) {
        this.metricKey = metricKey;
    }
}
