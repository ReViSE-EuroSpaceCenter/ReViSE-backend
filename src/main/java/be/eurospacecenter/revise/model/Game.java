package be.eurospacecenter.revise.model;

import be.eurospacecenter.revise.exceptions.InvalidGameOperationException;

import java.util.Map;
import java.util.UUID;

public class Game {

    private final Host host;
    private final Map<UUID, Team> teams;

    public Game(Host host, Map<UUID, Team> teams) {
        this.host = host;
        this.teams = teams;
    }

    public Host getHost() {
        return host;
    }

    public void removeRessource(UUID id, ResourceType type, int amount) {
        try {
            Team team = teams.get(id);
            team.remove(type, amount);
        } catch (NullPointerException e) {
            throw new InvalidGameOperationException("Équipe introuvable");
        }

    }

    public int generalScore() {
        return teams.values().stream().mapToInt(Team::score).sum();
    }
}
