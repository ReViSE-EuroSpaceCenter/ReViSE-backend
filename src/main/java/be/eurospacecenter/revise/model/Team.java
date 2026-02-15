package be.eurospacecenter.revise.model;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

public class Team {
    private final UUID clientId;
    private TeamLabel label;

    private boolean firstBonusMissionCompleted;
    private boolean secondBonusMissionCompleted;

    private final Map<ResourceType, Resource> resources = new EnumMap<>(ResourceType.class);

    public Team(UUID clientId) {
        this.clientId = clientId;

        initResources();
    }

    public Team(TeamLabel label, UUID clientId) {
        this.label = label;
        this.clientId = clientId;

        initResources();
    }

    public String getLabel() {
        if (label == null) {
            return null;
        }

        return label.toString();
    }

    public void setLabel(TeamLabel label) {
        this.label = label;
    }

    public UUID getClientID() {
        return clientId;
    }

    public boolean hasLabel() {
        return label != null;
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

    private void initResources() {
        for (ResourceType type : ResourceType.values()) {
            resources.put(type, new Resource(type.max()));
        }
    }
}