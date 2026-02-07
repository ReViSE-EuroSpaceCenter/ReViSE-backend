package be.eurospacecenter.revise.model;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Lobby {

    private final String code;
    private final Host host;
    private final Map<UUID, Team> players = new ConcurrentHashMap<>();

    public Lobby(String code, Host host) {
        this.code = code;
        this.host = host;
    }

    public String getCode() {
        return code;
    }

    public Collection<Team> getPlayers() {
        return Collections.unmodifiableCollection(players.values());
    }

    public void addPlayer(Team team) {
        players.put(team.getId(), team);
    }

    public void teamLabelIsAvailable(String teamId) {
        boolean alreadyTaken = players.values().stream().anyMatch(team -> team.label().equals(teamId));

        if (alreadyTaken) {
            throw new IllegalArgumentException("Cette équipe est déjà pris");
        }
    }

    public boolean isHost(UUID id) {
        return host.id().equals(id);
    }
}