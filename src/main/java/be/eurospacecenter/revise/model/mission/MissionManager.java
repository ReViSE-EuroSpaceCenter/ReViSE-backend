package be.eurospacecenter.revise.model.mission;

import be.eurospacecenter.revise.exceptions.ErrorKeys;
import be.eurospacecenter.revise.exceptions.InvalidMissionOperationException;
import be.eurospacecenter.revise.exceptions.NoAutoriseOperationException;
import be.eurospacecenter.revise.model.*;
import be.eurospacecenter.revise.model.Team;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class MissionManager {

    private final GameInfo gameInfo;
    private boolean allTeamsMissionsCompleted = false;

    public MissionManager(GameInfo gameInfo) {
        this.gameInfo = gameInfo;
    }

    public GameInfo getGameInfo() {
        return gameInfo;
    }

    public boolean isAllTeamsMissionsCompleted() {
        return allTeamsMissionsCompleted;
    }

    public TeamProgression changeTeamMissionsState(UUID id, String teamLabel, List<MissionType> missions) {
        if (teamLabel == null) {
            return changeTeamMissionsState(id, missions);
        }

        if (gameInfo.isNotHost(id)) {
            throw new NoAutoriseOperationException(ErrorKeys.ACTION_RESERVED_TO_HOST);
        }

        Team team = gameInfo.getTeamByLabel(teamLabel);
        missions.forEach(team::updateMission);

        updateAllTeamsMissionsCompletion(team);

        return team.getProgression();
    }

    public TeamProgression changeTeamMissionsState(UUID clientId, List<MissionType> missions) {
        Team team = gameInfo.getTeam(clientId);
        missions.forEach(team::updateMission);

        updateAllTeamsMissionsCompletion(team);

        return team.getProgression();
    }

    public TeamProgression getTeamProgression(UUID id) {
        Team team = gameInfo.getTeam(id);
        return team.getProgression();
    }

    public TeamFullProgression getTeamFullProgression(UUID id) {
        Team team = gameInfo.getTeam(id);
        return team.getFullProgression();
    }

    public Map<String, TeamProgression> getTeamsProgression() {
        Map<UUID, Team> teams = gameInfo.getTeams();

        return teams.values().stream().collect(Collectors.toMap(Team::getLabel, Team::getProgression));
    }

    public void validateEndOfMission(UUID hostId) {
        if (gameInfo.isNotHost(hostId)) {
            throw new NoAutoriseOperationException(ErrorKeys.ACTION_RESERVED_TO_HOST);
        }

        if (!allTeamsMissionsCompleted) {
            throw new InvalidMissionOperationException(ErrorKeys.LAUNCHER_START_INCOMPLETE_MISSIONS);
        }
    }

    private boolean areAllTeamsMissionsCompleted() {
        return gameInfo.getTeams().values().stream().allMatch(Team::allClassicMissionsCompleted);
    }

    private void updateAllTeamsMissionsCompletion(Team team) {
        allTeamsMissionsCompleted = team.allClassicMissionsCompleted() && areAllTeamsMissionsCompleted();
    }
}
