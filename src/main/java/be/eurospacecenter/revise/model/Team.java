package be.eurospacecenter.revise.model;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

public class Team {
    private final TeamId label;
    private final UUID id;

    private boolean firstBonusMissionCompleted;
    private boolean secondBonusMissionCompleted;

    private final Map<ResourceType, Resource> resources = new EnumMap<>(ResourceType.class);

    public Team(TeamId label, UUID id) {
        this.label = label;
        this.id = id;
        for (ResourceType type : ResourceType.values()) {
            resources.put(type, new Resource(type.max()));
        }
    }

    public String label() {
        return label.label;
    }

    public UUID getId() {
        return id;
    }

    public boolean isFirstBonusMissionCompleted() {
        return firstBonusMissionCompleted;
    }

    public boolean isSecondBonusMissionCompleted() {
        return secondBonusMissionCompleted;
    }

    public void completeFirstBonusMission() {
        firstBonusMissionCompleted = true;
    }

    public void completeSecondBonusMission() {
        secondBonusMissionCompleted = true;
    }

    public void remove(ResourceType type, int amount) {
        resources.get(type).remove(amount);
    }

    public int score() {
        return resources.get(ResourceType.ENERGY).remaining() / 3
                + resources.get(ResourceType.HUMAN).remaining()
                + resources.get(ResourceType.CLOCK).remaining();
    }
}