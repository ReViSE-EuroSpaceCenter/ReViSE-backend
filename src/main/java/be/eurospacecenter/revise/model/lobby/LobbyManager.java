package be.eurospacecenter.revise.model.lobby;

import be.eurospacecenter.revise.exceptions.ErrorKeys;
import be.eurospacecenter.revise.exceptions.NoAutoriseOperationException;
import be.eurospacecenter.revise.model.GameInfo;
import be.eurospacecenter.revise.model.Team;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class LobbyManager {

    private static final int TEAM_COUNT_FOUR = 4;
    private static final int TEAM_COUNT_SIX = 6;

    private final GameInfo gameInfo;
    private final boolean isFourTeamsMode;

    public LobbyManager(Host host, int numberOfTeams, LocalDateTime createdAt) {
        validateTeamCount(numberOfTeams);

        this.gameInfo = new GameInfo(host, createdAt);
        this.isFourTeamsMode = numberOfTeams == TEAM_COUNT_FOUR;
    }

    public GameInfo getGameInfo() {
        return gameInfo;
    }

    public void addTeam(UUID clientId) {
        gameInfo.addTeam(new Team(clientId));
    }

    public void assignTeam(UUID clientId, TeamLabel teamLabel) {
        ensureClient(clientId);

        Team team = gameInfo.getTeam(clientId);

        validateTeamLabel(teamLabel);
        ensureTeamNotAlreadyAssigned(team);
        ensureLabelNotTaken(teamLabel);

        team.setLabel(teamLabel);
    }

    public Set<TeamLabel> getAvailableTeamLabels() {
        Map<UUID, Team> teams = gameInfo.getTeams();

        Set<TeamLabel> takenLabels = teams.values().stream().filter(Team::hasLabel).map(Team::getLabel).collect(Collectors.toSet());

        return TeamLabel.getAllowedLabels(isFourTeamsMode).stream().filter(label -> !takenLabels.contains(label)).collect(Collectors.toSet());
    }

    public Set<TeamLabel> getAllTeamLabels() {
        return TeamLabel.getAllowedLabels(isFourTeamsMode);
    }

    public boolean startGame(UUID hostId) {
        ensureHost(hostId);

        Map<UUID, Team> teams = gameInfo.getTeams();

        Set<TeamLabel> teamLabels = teams.values().stream().filter(Team::hasLabel).map(Team::getLabel).collect(Collectors.toSet());

        if (!TeamLabel.isValidTeams(teamLabels, isFourTeamsMode)) {
            throw new IllegalArgumentException(ErrorKeys.INVALID_TEAM_LABELS);
        }

        return true;
    }

    private void ensureHost(UUID hostId) {
        if (gameInfo.isNotHost(hostId)) {
            throw new NoAutoriseOperationException(ErrorKeys.ACTION_RESERVED_TO_HOST);
        }
    }

    private void ensureClient(UUID clientId) {
        if (gameInfo.isNotClient(clientId)) {
            throw new NoAutoriseOperationException(ErrorKeys.CLIENT_NOT_IN_LOBBY);
        }
    }

    private static void validateTeamCount(int teamCount) {
        if (teamCount != TEAM_COUNT_FOUR && teamCount != TEAM_COUNT_SIX) {
            throw new IllegalArgumentException(ErrorKeys.INVALID_NUMBER_OF_TEAMS);
        }
    }

    private void validateTeamLabel(TeamLabel teamLabel) {
        if (!TeamLabel.isValidLabel(teamLabel, isFourTeamsMode)) {
            throw new IllegalArgumentException(ErrorKeys.INVALID_TEAM_LABEL);
        }
    }

    private void ensureTeamNotAlreadyAssigned(Team team) {
        if (team.hasLabel()) {
            throw new IllegalArgumentException(ErrorKeys.CLIENT_ALREADY_CHOSE_TEAM);
        }
    }

    private void ensureLabelNotTaken(TeamLabel teamLabel) {
        Map<UUID, Team> teams = gameInfo.getTeams();

        boolean alreadyTaken = teams.values().stream().anyMatch(team -> teamLabel.equals(team.getLabel()));

        if (alreadyTaken) {
            throw new IllegalArgumentException(ErrorKeys.TEAM_LABEL_ALREADY_TAKEN);
        }
    }
}
