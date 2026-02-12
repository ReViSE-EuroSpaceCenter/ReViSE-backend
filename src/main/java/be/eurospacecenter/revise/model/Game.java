package be.eurospacecenter.revise.model;

import java.util.Map;
import java.util.UUID;

public class Game {

    private final Host host;
    private final Map<UUID, Team> teams;

    public Game(Host host, Map<UUID, Team> teams) {
        this.host = host;
        this.teams = teams;
    }

    public int generalScore() {
        return teams.values().stream().mapToInt(Team::score).sum();
    }
}
