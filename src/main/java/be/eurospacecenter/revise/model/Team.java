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

    private final Map<ResourceType, Integer> resources = new EnumMap<>(ResourceType.class);

    public Team(UUID clientId) {
        this.clientId = clientId;
    }

    public Team(TeamLabel label, UUID clientId) {
        this.label = label;
        this.clientId = clientId;
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
        ensureOnlyMecaCanCompleteClassic8(missionType);

        switch (missionType) {
            case CLASSIC_1, CLASSIC_2, CLASSIC_3, CLASSIC_4,
                 CLASSIC_5, CLASSIC_6, CLASSIC_7, CLASSIC_8 ->
                    missionsCompleted[missionType.ordinal()] = !missionsCompleted[missionType.ordinal()];

            case BONUS_1 -> firstBonusMissionCompleted = !firstBonusMissionCompleted;
            case BONUS_2 -> secondBonusMissionCompleted = !secondBonusMissionCompleted;
        }
    }

    public TeamProgression getProgression() {
        return new TeamProgression(getMissionCompletionPercentage(), firstBonusMissionCompleted, secondBonusMissionCompleted);
    }

    private void ensureOnlyMecaCanCompleteClassic8(MissionType missionType) {
        if (missionType == MissionType.CLASSIC_8 && label != TeamLabel.MECA) {
            throw new InvalidGameOperationException("Seule l'équipe MECA peut compléter la mission CLASSIC_8.");
        }
    }

    private int countCompletedMissions() {
        return (int) java.util.stream.IntStream.range(0, missionsCompleted.length)
                .filter(i -> missionsCompleted[i])
                .count();
    }

    private float getMissionCompletionPercentage() {
        int totalMissions = label == TeamLabel.MECA ? 8 : 7;
        int completedMissions = countCompletedMissions();
        return (float) completedMissions / totalMissions * 100;
    }
}