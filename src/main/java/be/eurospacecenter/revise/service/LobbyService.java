package be.eurospacecenter.revise.service;

import be.eurospacecenter.revise.exceptions.ErrorKeys;
import be.eurospacecenter.revise.exceptions.NotFoundException;
import be.eurospacecenter.revise.metric.MetricType;
import be.eurospacecenter.revise.metric.RecordMetric;
import be.eurospacecenter.revise.model.GameState;
import be.eurospacecenter.revise.model.lobby.*;
import be.eurospacecenter.revise.model.lobbycode.LobbyCode;
import be.eurospacecenter.revise.model.lobbycode.LobbyCodeGenerator;
import be.eurospacecenter.revise.notification.LobbyNotifier;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LobbyService implements Cleanable {
    // Will be used in US 1O7
    private static final GameState STATE = GameState.LOBBY;

    final Map<LobbyCode, Lobby> managers = new ConcurrentHashMap<>();

    private final LobbyCodeGenerator lobbyCodeGenerator;
    private final MissionService missionService;
    private final LobbyNotifier notifier;

    public LobbyService(MissionService missionService, LobbyNotifier notifier, LobbyCodeGenerator lobbyCodeGenerator) {
        this.missionService = missionService;
        this.notifier = notifier;
        this.lobbyCodeGenerator = lobbyCodeGenerator;
    }

    public Lobby getLobbyInfo(LobbyCode lobbyCode) {
        return getManager(lobbyCode);
    }

    @RecordMetric(MetricType.GAME_CREATED)
    public LobbyCreation createLobby(int numberOfTeams) {
        LobbyCode lobbyCode = lobbyCodeGenerator.generate();
        Host host = new Host(UUID.randomUUID());

        Lobby lobby = new Lobby(host, numberOfTeams, LocalDateTime.now());
        managers.put(lobbyCode, lobby);

        return new LobbyCreation(lobbyCode, host.id());
    }

    @RecordMetric(MetricType.GAME_JOINED)
    public LobbyJoined joinLobby(LobbyCode lobbyCode) {
        Lobby lobby = getManager(lobbyCode);
        UUID clientId = UUID.randomUUID();

        lobby.addTeam(clientId);
        notifier.notifyClientJoined(lobbyCode);

        return new LobbyJoined(clientId.toString(), lobby.getAvailableTeamLabels(), lobby.getAllTeamLabels());
    }

    public void assignTeam(LobbyCode lobbyCode, UUID clientId, TeamLabel teamLabel) {
        Lobby lobby = getManager(lobbyCode);

        lobby.assignTeam(clientId, teamLabel);

        notifier.notifyTeamJoined(lobbyCode, teamLabel);
    }

    @RecordMetric(MetricType.GAME_STARTED)
    public void startGame(LobbyCode lobbyCode, UUID hostId) {
        Lobby lobby = getManager(lobbyCode);

        lobby.startGame(hostId);
        missionService.registerManager(lobbyCode, lobby.getGameInfo());

        notifier.notifyGameStarted(lobbyCode);
    }

    void addLobby(LobbyCode lobbyCode, Lobby lobby) {
        managers.put(lobbyCode, lobby);
    }

    private Lobby getManager(LobbyCode lobbyCode) {
        return Optional.ofNullable(managers.get(lobbyCode)).orElseThrow(() -> new NotFoundException(ErrorKeys.LOBBY_NOT_FOUND));
    }

    @Override
    public void cleanUp(List<LobbyCode> toRemove) {
        toRemove.forEach(managers::remove);
    }
}