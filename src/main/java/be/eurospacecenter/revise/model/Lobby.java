package be.eurospacecenter.revise.model;

import be.eurospacecenter.revise.exceptions.InvalidStartLobbyException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Lobby {

    private final String code;
    private final Host host;
    private final Map<UUID, Team> teams = new ConcurrentHashMap<>();

    public Lobby(String code, Host host) {
        this.code = code;
        this.host = host;
    }

    public String getCode() {
        return code;
    }

    public List<Team> getTeams() {
        return Collections.unmodifiableCollection(teams.values()).stream().toList();
    }

    public void addTeam(Team team) {
        if (teamLabelTaken(team.label())) {
            throw new IllegalArgumentException("Cette équipe est déjà prise");
        }

        teams.put(team.getId(), team);
    }

    public void startGame(UUID hostId) {
        validHostId(hostId);
        validNumberOfTeams();

        // Logique pour démarrer la partie
        // Par exemple, déléguer à un objet GameManager ou similaire
    }

    public UUID getHostId() {
        return host.id();
    }

    private boolean teamLabelTaken(String teamId) {
        return teams.values().stream().anyMatch(team -> team.label().equals(teamId));
    }

    private void validHostId(UUID hostId) {
        if (!host.id().equals(hostId)) {
            throw new InvalidStartLobbyException("Seul l'hôte peut démarrer la partie");
        }
    }

    private void validNumberOfTeams() {
        int numberOfTeams = teams.size();

        if (numberOfTeams != 4 && numberOfTeams != 6) {
            throw new InvalidStartLobbyException("Le nombre d'équipes doit être de 4 ou 6 pour démarrer la partie");
        }

        if (numberOfTeams == 4) {
            validateFourTeamsLabels();
        }
    }

    private void validateFourTeamsLabels() {
        Set<String> validLabels = Set.of("INGE", "MEDI", "COOP", "MECA");

        for (Team team : teams.values()) {
            if (!validLabels.contains(team.label())) {
                throw new InvalidStartLobbyException("Pour 4 équipes, les labels doivent être INGE, MEDI, COOP et MECA");
            }
        }
    }
}