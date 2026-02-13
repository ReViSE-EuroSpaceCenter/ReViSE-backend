package be.eurospacecenter.revise.model;

import be.eurospacecenter.revise.exceptions.InvalidStartLobbyException;
import be.eurospacecenter.revise.exceptions.NotFoundException;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class Lobby {

    private static final int TEAM_COUNT_FOUR = 4;
    private static final int TEAM_COUNT_SIX = 6;

    private final Host host;
    private final Map<UUID, Team> teams;
    private final boolean isFourTeamsMode;

    public Lobby(Host host, int numberOfTeams) {
        validateTeamCount(numberOfTeams);

        this.host = Objects.requireNonNull(host);
        this.teams = new ConcurrentHashMap<>();
        this.isFourTeamsMode = numberOfTeams == TEAM_COUNT_FOUR;
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
        Set<String> takenLabels = teams.values().stream()
                .filter(Team::hasLabel)
                .map(Team::getLabel)
                .collect(Collectors.toSet());

        return TeamLabel.getAllowedLabels(isFourTeamsMode).stream()
                .map(Enum::name)
                .filter(label -> !takenLabels.contains(label))
                .toList();
    }

    public void startGame(UUID hostId) {
        ensureHost(hostId);

        List<String> teamLabels = teams.values().stream()
                .filter(Team::hasLabel)
                .map(Team::getLabel)
                .toList();

        if (!TeamLabel.isValidTeams(teamLabels, isFourTeamsMode)) {
            throw new InvalidStartLobbyException(
                    "Le nombre d'équipes ou les labels ne sont pas valides pour démarrer la partie"
            );
        }
    }

    public boolean isInLobby(UUID clientId) {
        return teams.containsKey(clientId);
    }

    /* ======================
       ====== Helpers =======
       ====================== */

    private static void validateTeamCount(int teamCount) {
        if (teamCount != TEAM_COUNT_FOUR && teamCount != TEAM_COUNT_SIX) {
            throw new IllegalArgumentException("Le nombre d'équipes doit être de 4 ou 6");
        }
    }

    private Team getTeam(UUID clientId) {
        Team team = teams.get(clientId);
        if (team == null) {
            throw new NotFoundException("Client non trouvé dans le lobby");
        }
        return team;
    }

    private void validateTeamLabel(String teamLabel) {
        if (!TeamLabel.isValidLabel(teamLabel, isFourTeamsMode)) {
            throw new IllegalArgumentException("Label d'équipe invalide : " + teamLabel);
        }
    }

    private void ensureTeamNotAlreadyAssigned(Team team) {
        if (team.hasLabel()) {
            throw new IllegalArgumentException("Ce client a déjà une équipe assignée");
        }
    }

    private void ensureLabelNotTaken(String teamLabel) {
        boolean alreadyTaken = teams.values().stream()
                .anyMatch(team -> teamLabel.equals(team.getLabel()));

        if (alreadyTaken) {
            throw new IllegalArgumentException("Cette équipe est déjà prise");
        }
    }

    private void ensureHost(UUID hostId) {
        if (!host.id().equals(hostId)) {
            throw new InvalidStartLobbyException("Seul l'hôte peut démarrer la partie");
        }
    }
}
