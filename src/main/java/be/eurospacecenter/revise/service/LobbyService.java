package be.eurospacecenter.revise.service;

import be.eurospacecenter.revise.exceptions.ErrorKeys;
import be.eurospacecenter.revise.exceptions.InvalidGameStateException;
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
    private static final GameState STATE = GameState.LOBBY;

    final Map<LobbyCode, LobbyManager> managers = new ConcurrentHashMap<>();

    private final LobbyCodeGenerator lobbyCodeGenerator;
    private final MissionService missionService;
    private final LobbyNotifier notifier;

    public LobbyService(MissionService missionService, LobbyNotifier notifier, LobbyCodeGenerator lobbyCodeGenerator) {
        this.missionService = missionService;
        this.notifier = notifier;
        this.lobbyCodeGenerator = lobbyCodeGenerator;
    }

    public LobbyManager getLobbyInfo(LobbyCode lobbyCode) {
        return getManager(lobbyCode);
    }

    @RecordMetric(MetricType.GAME_CREATED)
    public LobbyCreation createLobby(int numberOfTeams) {
        LobbyCode lobbyCode = lobbyCodeGenerator.generate();
        Host host = new Host(UUID.randomUUID());

        LobbyManager lobbyManager = new LobbyManager(host, numberOfTeams, LocalDateTime.now());
        managers.put(lobbyCode, lobbyManager);

        return new LobbyCreation(lobbyCode, host.id());
    }

    @RecordMetric(MetricType.GAME_JOINED)
    public LobbyJoined joinLobby(LobbyCode lobbyCode) {
        LobbyManager lobbyManager = getManager(lobbyCode);
        UUID clientId = UUID.randomUUID();

        lobbyManager.addTeam(clientId);
        notifier.notifyClientJoined(lobbyCode);

        return new LobbyJoined(clientId.toString(), lobbyManager.getAvailableTeamLabels(), lobbyManager.getAllTeamLabels());
    }

    public void assignTeam(LobbyCode lobbyCode, UUID clientId, TeamLabel teamLabel) {
        LobbyManager lobbyManager = getManager(lobbyCode);

        lobbyManager.assignTeam(clientId, teamLabel);

        notifier.notifyTeamJoined(lobbyCode, teamLabel);
    }

    @RecordMetric(MetricType.GAME_STARTED)
    public void startGame(LobbyCode lobbyCode, UUID hostId) {
        LobbyManager lobbyManager = getManager(lobbyCode);

        lobbyManager.startGame(hostId);
        missionService.registerManager(lobbyCode, lobbyManager.getGameInfo());

        notifier.notifyGameStarted(lobbyCode);
    }

    @Override
    public void cleanUp(List<LobbyCode> toRemove) {
        toRemove.forEach(managers::remove);
    }

    void addLobby(LobbyCode lobbyCode, LobbyManager lobbyManager) {
        managers.put(lobbyCode, lobbyManager);
    }

    private LobbyManager getManager(LobbyCode lobbyCode) {
        LobbyManager manager = Optional.ofNullable(managers.get(lobbyCode))
                .orElseThrow(() -> new NotFoundException(ErrorKeys.LOBBY_MANAGER_NOT_FOUND));

        GameState currentState = manager.getGameInfo().getState();

        if (currentState != STATE) {
            throw new InvalidGameStateException(currentState);
        }

        return manager;
    }
}