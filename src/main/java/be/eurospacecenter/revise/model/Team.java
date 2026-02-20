package be.eurospacecenter.revise.model;

import be.eurospacecenter.revise.exceptions.InvalidGameOperationException;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

public class Team {
    private final UUID clientId;
    private TeamLabel label;

    private final boolean[] missionsCompleted = new boolean[8];

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

    public void changeMissionState(MissionType missionType) {
        validateClassic8Access(missionType);

        switch (missionType) {
            case CLASSIC_1, CLASSIC_2, CLASSIC_3, CLASSIC_4,
                 CLASSIC_5, CLASSIC_6, CLASSIC_7, CLASSIC_8 ->
                    missionsCompleted[missionType.ordinal()] = !missionsCompleted[missionType.ordinal()];

            case BONUS_1 -> firstBonusMissionCompleted = !firstBonusMissionCompleted;
            case BONUS_2 -> secondBonusMissionCompleted = !secondBonusMissionCompleted;
        }
    }

    public boolean isMissionBonusCompleted(MissionType missionType) {
        return switch (missionType) {
            case BONUS_1 -> firstBonusMissionCompleted;
            case BONUS_2 -> secondBonusMissionCompleted;
            default -> throw new IllegalArgumentException(
                    "Mission non bonus : " + missionType
            );
        };
    }

    public float getMissionCompletionPercentage() {
        int totalMissions = label == TeamLabel.MECA ? 8 : 7;
        int completedMissions = countCompletedMissions();
        return (float) completedMissions / totalMissions * 100;
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

    private void validateClassic8Access(MissionType missionType) {
        if (missionType == MissionType.CLASSIC_8 && label != TeamLabel.MECA) {
            throw new InvalidGameOperationException("Seule l'équipe MECA peut compléter la mission CLASSIC_8.");
        }
    }

    private int countCompletedMissions() {
        return (int) java.util.stream.IntStream.range(0, missionsCompleted.length)
                .filter(i -> missionsCompleted[i])
                .count();
    }
}