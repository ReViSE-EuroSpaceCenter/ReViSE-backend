package be.eurospacecenter.revise.model.resource;

public enum ResourceType {
    ENERGY(40),
    HUMAN(6),
    CLOCK(14);

    private final int max;

    ResourceType(int max) {
        this.max = max;
    }

    public int getMax() {
        return max;
    }
}
