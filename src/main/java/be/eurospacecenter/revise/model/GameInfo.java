package be.eurospacecenter.revise.model;

import be.eurospacecenter.revise.exceptions.ErrorKeys;
import be.eurospacecenter.revise.exceptions.NotFoundException;
import be.eurospacecenter.revise.model.lobby.Host;
import be.eurospacecenter.revise.model.lobby.TeamLabel;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class GameInfo {

    private static final int LOBBY_TTL = 12;

    private final Host host;
    private final Map<UUID, Team> teams;
    private final LocalDateTime expiresAt;

    public GameInfo(Host host, LocalDateTime createdAt) {
        this.host = Objects.requireNonNull(host);
        this.teams = new ConcurrentHashMap<>();
        this.expiresAt = createdAt.plusHours(LOBBY_TTL);
    }

    public void addTeam(Team team) {
        teams.put(team.getClientID(), team);
    }

    public Team getTeam(UUID clientId) {
        return Optional.ofNullable(teams.get(clientId)).orElseThrow(() -> new NotFoundException(ErrorKeys.TEAM_NOT_FOUND));
    }

    public Team getTeamByLabel(TeamLabel teamLabel) {
        return teams.values().stream().filter(team -> team.getLabel().equals(teamLabel)).findFirst().orElseThrow(() -> new NotFoundException(ErrorKeys.TEAM_NOT_FOUND));
    }

    public Map<UUID, Team> getTeams() {
        return Map.copyOf(teams);
    }

    public boolean isNotHost(UUID hostId) {
        return !host.id().equals(hostId);
    }

    public boolean isNotClient(UUID clientId) {
        return !teams.containsKey(clientId);
    }

    public LocalDateTime getExpiresAt() {
        return this.expiresAt;
    }

    public UUID getHostId() {
        return host.id();
    }

    public int getTotalScore() {
        return teams.values().stream()
                .mapToInt(Team::score)
                .sum();
    }
}
