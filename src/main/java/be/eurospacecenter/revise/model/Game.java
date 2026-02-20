package be.eurospacecenter.revise.model;

import be.eurospacecenter.revise.exceptions.InvalidGameOperationException;

import java.util.Map;
import java.util.UUID;

public class Game {

    private final Map<UUID, Team> teams;

    public Game(Map<UUID, Team> teams) {
        this.teams = teams;
    }
    
    public String getTeamLabel(UUID id) {
        return teams.get(id).getLabel();
    }

    public void changeTeamMissionState(UUID id, MissionType missionType) {
        try {
            Team team = teams.get(id);
            team.changeMissionState(missionType);
        } catch (NullPointerException e) {
            throw new InvalidGameOperationException("Équipe introuvable");
        }
    }

    public TeamProgression getTeamProgression(UUID id) {
        try {
            Team team = teams.get(id);
            boolean firstBonusMissionCompleted = team.isMissionBonusCompleted(MissionType.BONUS_1);
            boolean secondBonusMissionCompleted = team.isMissionBonusCompleted(MissionType.BONUS_2);
            float classicMissionPercentage = team.getMissionCompletionPercentage();
            return new TeamProgression(classicMissionPercentage, firstBonusMissionCompleted, secondBonusMissionCompleted);
        } catch (NullPointerException e) {
            throw new InvalidGameOperationException("Équipe introuvable");
        }
    }

    public int generalScore() {
        return teams.values().stream().mapToInt(Team::score).sum();
    }
}
