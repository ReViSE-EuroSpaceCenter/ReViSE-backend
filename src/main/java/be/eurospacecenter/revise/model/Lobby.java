package be.eurospacecenter.revise.model;

import be.eurospacecenter.revise.exceptions.ErrorKeys;
import be.eurospacecenter.revise.exceptions.NoAutoriseOperationException;
import be.eurospacecenter.revise.exceptions.NotFoundException;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class Lobby {

    private static final int TEAM_COUNT_FOUR = 4;
    private static final int TEAM_COUNT_SIX = 6;
    private static final int LOBBY_TTL = 12;

    private final Host host;
    private final Map<UUID, Team> teams;
    private final boolean isFourTeamsMode;
    private final LocalDateTime expiresAt;

    public Lobby(Host host, int numberOfTeams, LocalDateTime createdAt) {
        validateTeamCount(numberOfTeams);

        this.host = Objects.requireNonNull(host);
        this.teams = new ConcurrentHashMap<>();
        this.isFourTeamsMode = numberOfTeams == TEAM_COUNT_FOUR;

        this.expiresAt = createdAt.plusHours(LOBBY_TTL);
    }

    public Map<UUID, Team> getTeams() {
        return Collections.unmodifiableMap(teams);
    }

    public void addTeam(Team team) {
        teams.put(team.getClientID(), team);
    }

    public void assignTeam(UUID clientId, String teamLabel) {
        Team team = getTeam(clientId);

        validateTeamLabel(teamLabel);
        ensureTeamNotAlreadyAssigned(team);
        ensureLabelNotTaken(teamLabel);

        team.setLabel(TeamLabel.valueOf(teamLabel));
    }

    public List<String> getFreeTeamLabels() {
        Set<String> takenLabels = teams.values().stream().filter(Team::hasLabel).map(Team::getLabel).collect(Collectors.toSet());

        return TeamLabel.getAllowedLabels(isFourTeamsMode).stream().map(Enum::name).filter(label -> !takenLabels.contains(label)).toList();
    }

    public List<String> getAllTeamLabels() {
        return TeamLabel.getAllowedLabels(isFourTeamsMode).stream().map(Enum::name).toList();
    }

    public boolean startGame(UUID hostId) {
        if (isNotHost(hostId)) {
            throw new NoAutoriseOperationException(ErrorKeys.ACTION_RESERVED_TO_HOST);
        }

        List<String> teamLabels = teams.values().stream().filter(Team::hasLabel).map(Team::getLabel).toList();

        if (!TeamLabel.isValidTeams(teamLabels, isFourTeamsMode)) {
            throw new IllegalArgumentException(ErrorKeys.INVALID_TEAM_LABELS);
        }

        return true;
    }

    public boolean isClient(UUID clientId) {
        return teams.containsKey(clientId);
    }

    public boolean isNotHost(UUID hostId) {
        return !host.id().equals(hostId);
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

    private Team getTeam(UUID id) {
        return Optional.ofNullable(teams.get(id)).orElseThrow(() -> new NotFoundException(ErrorKeys.TEAM_NOT_FOUND));
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
        boolean alreadyTaken = teams.values().stream().anyMatch(team -> teamLabel.equals(team.getLabel()));

        if (alreadyTaken) {
            throw new IllegalArgumentException(ErrorKeys.TEAM_LABEL_ALREADY_TAKEN);
        }
    }
}
