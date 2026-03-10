package be.eurospacecenter.revise.model.lobby;

import be.eurospacecenter.revise.exceptions.ErrorKeys;
import be.eurospacecenter.revise.exceptions.InvalidLauncherOperationException;
import be.eurospacecenter.revise.exceptions.InvalidMissionOperationException;
import be.eurospacecenter.revise.model.launcher.ResourceType;
import be.eurospacecenter.revise.model.launcher.TeamResources;
import be.eurospacecenter.revise.model.mission.MissionType;
import be.eurospacecenter.revise.model.mission.TeamFullProgression;
import be.eurospacecenter.revise.model.mission.TeamProgression;

import java.util.EnumMap;
import java.util.HashMap;
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
        ensureOnlyMecaCanCompleteClassic8(missionType);

        switch (missionType) {
            case CLASSIC_1, CLASSIC_2, CLASSIC_3, CLASSIC_4, CLASSIC_5, CLASSIC_6, CLASSIC_7, CLASSIC_8 ->
                    missionsCompleted[missionType.ordinal()] = !missionsCompleted[missionType.ordinal()];

            case BONUS_1 -> firstBonusMissionCompleted = !firstBonusMissionCompleted;
            case BONUS_2 -> secondBonusMissionCompleted = !secondBonusMissionCompleted;
        }
    }

    public TeamProgression getProgression() {
        return new TeamProgression(countCompletedMissions(), firstBonusMissionCompleted, secondBonusMissionCompleted);
    }

    public TeamFullProgression getFullProgression() {
        TeamProgression baseProgression = getProgression();

        Map<String, Boolean> missionStatusMap = new HashMap<>();

        MissionType.getClassicMissions().stream().toList().forEach(missionType -> missionStatusMap.put(missionType.name(), missionsCompleted[missionType.ordinal()]));

        if (label != TeamLabel.MECA) {
            missionStatusMap.remove(MissionType.CLASSIC_8.name());
        }

        return new TeamFullProgression(missionStatusMap, baseProgression);
    }

    public boolean allClassicMissionsCompleted() {
        int totalMissions = label == TeamLabel.MECA ? 8 : 7;
        int completedMissions = countCompletedMissions();

        return completedMissions == totalMissions;
    }

    public TeamResources updateResources(Map<ResourceType, Integer> toRemove) {
        removeResources(toRemove);
        return new TeamResources(label, resources);
    }

    private void removeResources(Map<ResourceType, Integer> toRemove) {
        checkSufficientResources(toRemove);
        toRemove.forEach((type, amount) ->
                resources.merge(type, -amount, Integer::sum)
        );
    }

    public int score() {
        return resources.get(ResourceType.ENERGY) / 3
                + resources.get(ResourceType.HUMAN)
                + resources.get(ResourceType.CLOCK);
    }

        private void initResources() {
            for (ResourceType type : ResourceType.values()) {
                resources.put(type, type.getMax());
            }
        }

    private void ensureOnlyMecaCanCompleteClassic8(MissionType missionType) {
        if (missionType == MissionType.CLASSIC_8 && label != TeamLabel.MECA) {
            throw new InvalidMissionOperationException(ErrorKeys.ONLY_MECA_COMPLETE_CLASSIC_8);
        }
    }

    private int countCompletedMissions() {
        int count = 0;
        for (boolean completed : missionsCompleted) {
            if (completed) count++;
        }
        return count;
    }

    private void checkSufficientResources(Map<ResourceType, Integer> toRemove) {
        for (Map.Entry<ResourceType, Integer> entry : toRemove.entrySet()) {
            int current = resources.getOrDefault(entry.getKey(), 0);
            int required = entry.getValue();

            if (current < required) {
                throw new InvalidLauncherOperationException(ErrorKeys.INSUFFICIENT_RESOURCES);
            }
        }
    }
}