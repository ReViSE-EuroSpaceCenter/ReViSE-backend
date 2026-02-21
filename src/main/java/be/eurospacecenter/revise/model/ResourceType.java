package be.eurospacecenter.revise.model;

public enum ResourceType {
    ENERGY(40),
    HUMAN(6),
    CLOCK(6);

    private final int max;

    ResourceType(int max) {
        this.max = max;
    }
}

