package be.eurospacecenter.revise.model;

import be.eurospacecenter.revise.exceptions.InvalidHostIdException;
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
        if (!host.id().equals(hostId)) {
            throw new InvalidHostIdException("Seul l'hôte peut démarrer la partie");
        }

        // Logique pour démarrer la partie
        // Par exemple, déléguer à un objet GameManager ou similaire
    }

    public UUID getHostId() {
        return host.id();
    }

    private boolean teamLabelTaken(String teamId) {
        return teams.values().stream().anyMatch(team -> team.label().equals(teamId));
    }
}