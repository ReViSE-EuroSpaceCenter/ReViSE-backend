package be.eurospacecenter.revise.model;

import be.eurospacecenter.revise.exceptions.ErrorKeys;
import be.eurospacecenter.revise.exceptions.NotFoundException;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class GameInfo {
    private final Host host;
    private final Map<UUID, Team> teams;

    public GameInfo(Host host) {
        this.host = Objects.requireNonNull(host);
        this.teams = new ConcurrentHashMap<>();
    }

    public void addTeam(Team team) {
        teams.put(team.getClientID(), team);
    }

    public Map<UUID, Team> getTeams() {
        return Map.copyOf(teams);
    }

    public Team getTeam(UUID clientId) {
        return Optional.ofNullable(teams.get(clientId)).orElseThrow(() -> new NotFoundException(ErrorKeys.TEAM_NOT_FOUND));
    }

    public boolean isNotHost(UUID hostId) {
        return !host.id().equals(hostId);
    }

    public boolean isNotClient(UUID clientId) {
        return !teams.containsKey(clientId);
    }
}
