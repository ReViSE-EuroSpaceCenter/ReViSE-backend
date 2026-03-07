package be.eurospacecenter.revise.model;

import be.eurospacecenter.revise.exceptions.ErrorKeys;
import be.eurospacecenter.revise.exceptions.NoAutoriseOperationException;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class Lobby {

    private static final int TEAM_COUNT_FOUR = 4;
    private static final int TEAM_COUNT_SIX = 6;
    private static final int LOBBY_TTL = 12;

    private final GameInfo gameInfo;
    private final boolean isFourTeamsMode;
    private final LocalDateTime expiresAt;

    public Lobby(Host host, int numberOfTeams, LocalDateTime createdAt) {
        validateTeamCount(numberOfTeams);

        this.gameInfo = new GameInfo(host);
        this.isFourTeamsMode = numberOfTeams == TEAM_COUNT_FOUR;

        this.expiresAt = createdAt.plusHours(LOBBY_TTL);
    }

    public GameInfo getGameInfo() {
        return gameInfo;
    }

    public void addTeam(UUID clientId) {
        gameInfo.addTeam(new Team(clientId));
    }

    public void assignTeam(UUID clientId, String teamLabel) {
        if (gameInfo.isNotClient(clientId)) {
            throw new NoAutoriseOperationException(ErrorKeys.CLIENT_NOT_IN_LOBBY);
        }

        Team team = gameInfo.getTeam(clientId);

        validateTeamLabel(teamLabel);
        ensureTeamNotAlreadyAssigned(team);
        ensureLabelNotTaken(teamLabel);

        team.setLabel(TeamLabel.valueOf(teamLabel));
    }

    public List<String> getFreeTeamLabels() {
        Map<UUID, Team> teams = gameInfo.getTeams();

        Set<String> takenLabels = teams.values().stream().filter(Team::hasLabel).map(Team::getLabel).collect(Collectors.toSet());

        return TeamLabel.getAllowedLabels(isFourTeamsMode).stream().map(Enum::name).filter(label -> !takenLabels.contains(label)).toList();
    }

    public List<String> getAllTeamLabels() {
        return TeamLabel.getAllowedLabels(isFourTeamsMode).stream().map(Enum::name).toList();
    }

    public boolean startGame(UUID hostId) {
        if (gameInfo.isNotHost(hostId)) {
            throw new NoAutoriseOperationException(ErrorKeys.ACTION_RESERVED_TO_HOST);
        }

        Map<UUID, Team> teams = gameInfo.getTeams();

        List<String> teamLabels = teams.values().stream().filter(Team::hasLabel).map(Team::getLabel).toList();

        if (!TeamLabel.isValidTeams(teamLabels, isFourTeamsMode)) {
            throw new IllegalArgumentException(ErrorKeys.INVALID_TEAM_LABELS);
        }

        return true;
    }

    public LocalDateTime getExpiresAt() {
        return this.expiresAt;
    }

    /* ======================
       ====== Helpers =======
       ====================== */

    private static void validateTeamCount(int teamCount) {
        if (teamCount != TEAM_COUNT_FOUR && teamCount != TEAM_COUNT_SIX) {
            throw new IllegalArgumentException(ErrorKeys.INVALID_NUMBER_OF_TEAMS);
        }
    }

    private void validateTeamLabel(String teamLabel) {
        if (!TeamLabel.isValidLabel(teamLabel, isFourTeamsMode)) {
            throw new IllegalArgumentException(ErrorKeys.INVALID_TEAM_LABEL);
        }
    }

    private void ensureTeamNotAlreadyAssigned(Team team) {
        if (team.hasLabel()) {
            throw new IllegalArgumentException(ErrorKeys.CLIENT_ALREADY_CHOSE_TEAM);
        }
    }

    private void ensureLabelNotTaken(String teamLabel) {
        Map<UUID, Team> teams = gameInfo.getTeams();

        boolean alreadyTaken = teams.values().stream().anyMatch(team -> teamLabel.equals(team.getLabel()));

        if (alreadyTaken) {
            throw new IllegalArgumentException(ErrorKeys.TEAM_LABEL_ALREADY_TAKEN);
        }
    }
}
