package be.eurospacecenter.revise.model;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class Game {

    private final GameInfo gameInfo;

    public Game(GameInfo gameInfo) {
        this.gameInfo = gameInfo;
    }

    public String getTeamLabel(UUID id) {
        return gameInfo.getTeam(id).getLabel();
    }

    public void changeTeamMissionState(UUID id, MissionType missionType) {
        Team team = gameInfo.getTeam(id);
        team.changeMissionState(missionType);
    }

    public Map<String, TeamProgression> teamsProgression() {
        Map<UUID, Team> teams = gameInfo.getTeams();

        return teams.values().stream().collect(Collectors.toMap(Team::getLabel, Team::getProgression));
    }

    public TeamProgression getTeamProgression(UUID id) {
        Team team = gameInfo.getTeam(id);
        return team.getProgression();
    }

    public TeamFullProgression getTeamFullProgression(UUID id) {
        Team team = gameInfo.getTeam(id);

        return team.getFullProgression();
    }

    public void endMission(UUID hostId) {
    }
}
