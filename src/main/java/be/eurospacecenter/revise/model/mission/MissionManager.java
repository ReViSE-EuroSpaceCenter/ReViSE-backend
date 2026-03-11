package be.eurospacecenter.revise.model.mission;

import be.eurospacecenter.revise.exceptions.ErrorKeys;
import be.eurospacecenter.revise.exceptions.InvalidMissionOperationException;
import be.eurospacecenter.revise.model.*;
import be.eurospacecenter.revise.model.lobby.Team;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class MissionManager {

    private final GameInfo gameInfo;

    public MissionManager(GameInfo gameInfo) {
        this.gameInfo = gameInfo;
    }

    public GameInfo getGameInfo() {
        return gameInfo;
    }

    public TeamProgression changeTeamMissionsState(UUID id, String teamLabel, List<MissionType> missions) {
        if (teamLabel == null) {
            return changeTeamMissionsState(id, missions);
        }

        if (gameInfo.isNotHost(id)) {
            throw new IllegalArgumentException(ErrorKeys.ACTION_RESERVED_TO_HOST);
        }

        Team team = gameInfo.getTeamByLabel(teamLabel);
        missions.forEach(team::changeMissionState);

        return team.getProgression();
    }

    public TeamProgression changeTeamMissionsState(UUID clientId, List<MissionType> missions) {
        Team team = gameInfo.getTeam(clientId);
        missions.forEach(team::changeMissionState);

        return team.getProgression();
    }

    public Map<String, TeamProgression> teamsProgression() {
        Map<UUID, Team> teams = gameInfo.getTeams();

        return teams.values().stream().collect(Collectors.toMap(Team::getLabel, Team::getProgression));
    }

    public boolean allTeamsCompleted() {
        return gameInfo.getTeams().values().stream().allMatch(Team::allClassicMissionsCompleted);
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
        if (gameInfo.isNotHost(hostId)) {
            throw new IllegalArgumentException(ErrorKeys.ACTION_RESERVED_TO_HOST);
        }

        Map<UUID, Team> teams = gameInfo.getTeams();
        teams.forEach((id, team) -> {
            if (!team.allClassicMissionsCompleted()) {
                throw new InvalidMissionOperationException(ErrorKeys.LAUNCHER_START_INCOMPLETE_MISSIONS);
            }
        });

    }
}
