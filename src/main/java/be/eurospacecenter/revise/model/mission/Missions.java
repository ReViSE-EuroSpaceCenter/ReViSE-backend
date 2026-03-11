package be.eurospacecenter.revise.model.mission;

import be.eurospacecenter.revise.exceptions.ErrorKeys;
import be.eurospacecenter.revise.exceptions.InvalidMissionOperationException;
import be.eurospacecenter.revise.model.lobby.TeamLabel;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class Missions {

    private final Map<MissionType, Boolean> missionsMap = new EnumMap<>(MissionType.class);

    public Missions() {
        for (MissionType missionType : MissionType.values()) {
            missionsMap.put(missionType, false);
        }
    }

    public void update(TeamLabel teamLabel, MissionType missionType) {
        ensureOnlyMecaCanCompleteClassic8(teamLabel, missionType);
        missionsMap.computeIfPresent(missionType, (m, completed) -> !completed);
    }

    public TeamProgression getProgression(TeamLabel teamLabel) {
        return new TeamProgression(teamLabel, countCompletedMissions(), missionsMap.get(MissionType.BONUS_1), missionsMap.get(MissionType.BONUS_2));
    }

    public TeamFullProgression getFullProgression(TeamLabel teamLabel) {
        TeamProgression baseProgression = getProgression(teamLabel);

        Map<String, Boolean> missionStatusMap = new HashMap<>();

        MissionType.getClassicMissions().forEach(missionType -> missionStatusMap.put(missionType.name(), missionsMap.get(missionType)));

        if (teamLabel != TeamLabel.MECA) {
            missionStatusMap.remove(MissionType.CLASSIC_8.name());
        }

        return new TeamFullProgression(missionStatusMap, baseProgression);
    }

    public boolean allClassicMissionsCompleted(TeamLabel teamLabel) {
        int totalMissions = teamLabel == TeamLabel.MECA ? 8 : 7;
        int completedMissions = countCompletedMissions();

        return completedMissions == totalMissions;
    }

    private void ensureOnlyMecaCanCompleteClassic8(TeamLabel label, MissionType missionType) {
        if (missionType == MissionType.CLASSIC_8 && label != TeamLabel.MECA) {
            throw new InvalidMissionOperationException(ErrorKeys.ONLY_MECA_COMPLETE_CLASSIC_8);
        }
    }

    private int countCompletedMissions() {
        int count = 0;

        for (MissionType mission : MissionType.getClassicMissions()) {
            if (Boolean.TRUE.equals(missionsMap.get(mission))) {
                count++;
            }
        }

        return count;
    }
}