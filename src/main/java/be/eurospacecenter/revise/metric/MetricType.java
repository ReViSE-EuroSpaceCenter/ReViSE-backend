package be.eurospacecenter.revise.metric;

public enum MetricType {
    LOBBY_CREATED("lobby_created_counter"),
    LOBBY_JOINED("lobby_joined_counter"),
    LOBBY_STARTED("lobby_started_counter"),
    LOBBY_CLEARED("lobby_cleared_counter");

    public final String metricKey;

    MetricType(String metricKey) {
        this.metricKey = metricKey;
    }
}
