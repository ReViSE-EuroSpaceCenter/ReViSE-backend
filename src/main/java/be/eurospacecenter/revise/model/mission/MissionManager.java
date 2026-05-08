package be.eurospacecenter.revise.model.mission;

import be.eurospacecenter.revise.exceptions.ErrorKeys;
import be.eurospacecenter.revise.exceptions.InvalidMissionOperationException;
import be.eurospacecenter.revise.exceptions.NoAutoriseOperationException;
import be.eurospacecenter.revise.model.*;
import be.eurospacecenter.revise.model.Team;
import be.eurospacecenter.revise.model.lobby.TeamLabel;

import java.util.Map;
import java.util.Set;
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

    public TeamProgression changeTeamMissionsState(UUID id, TeamLabel teamLabel, Set<MissionType> missions) {
        if (teamLabel == null) {
            return changeTeamMissionsState(id, missions);
        }

        if (gameInfo.isNotHost(id)) {
            throw new NoAutoriseOperationException(ErrorKeys.ACTION_RESERVED_TO_HOST);
        }

        return applyMissionsUpdate(gameInfo.getTeamByLabel(teamLabel), missions);
    }

    public TeamProgression changeTeamMissionsState(UUID clientId, Set<MissionType> missions) {
        return applyMissionsUpdate(gameInfo.getTeam(clientId), missions);
    }

    public TeamProgression getTeamProgression(UUID id) {
        Team team = gameInfo.getTeam(id);
        return getTeamProgression(team);
    }

    public TeamFullProgression getTeamFullProgression(UUID id) {
        Team team = gameInfo.getTeam(id);

        return team.getFullProgression();
    }

    public TeamsProgression getTeamsFullProgression() {
        Map<UUID, Team> teams = gameInfo.getTeams();

        return new TeamsProgression(
                teams.values().stream().collect(Collectors.toMap(Team::getLabel, Team::getFullProgression)),
                allTeamsMissionsCompleted
        );
    }

    public void validateEndOfMission(UUID hostId) {
        if (gameInfo.isNotHost(hostId)) {
            throw new NoAutoriseOperationException(ErrorKeys.ACTION_RESERVED_TO_HOST);
        }

        if (!allTeamsMissionsCompleted) {
            throw new InvalidMissionOperationException(ErrorKeys.DISCOVER_START_INCOMPLETE_MISSIONS);
        }
    }

    private TeamProgression applyMissionsUpdate(Team team, Set<MissionType> missions) {
        missions.forEach(team::updateMission);
        updateAllTeamsMissionsCompletion(team);
        return getTeamProgression(team);
    }

    private TeamProgression getTeamProgression(Team team) {
        TeamProgression teamProgression = team.getProgression();
        return new TeamProgression(
                team.getLabel(),
                teamProgression.classicMissionsCompleted(),
                teamProgression.firstBonusMissionCompleted(),
                teamProgression.secondBonusMissionCompleted(),
                allTeamsMissionsCompleted
        );
    }

    private boolean areAllTeamsMissionsCompleted() {
        return gameInfo.getTeams().values().stream().allMatch(Team::allClassicMissionsCompleted);
    }

    private void updateAllTeamsMissionsCompletion(Team team) {
        allTeamsMissionsCompleted = team.allClassicMissionsCompleted() && areAllTeamsMissionsCompleted();
    }
}
