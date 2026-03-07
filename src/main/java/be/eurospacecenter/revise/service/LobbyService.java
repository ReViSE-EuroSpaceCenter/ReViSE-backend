package be.eurospacecenter.revise.service;

import be.eurospacecenter.revise.dto.response.LobbyCreationResponse;
import be.eurospacecenter.revise.dto.response.LobbyInfoResponse;
import be.eurospacecenter.revise.dto.response.LobbyJoinedResponse;
import be.eurospacecenter.revise.exceptions.ErrorKeys;
import be.eurospacecenter.revise.exceptions.NotFoundException;
import be.eurospacecenter.revise.model.lobby.Host;
import be.eurospacecenter.revise.model.lobby.Lobby;
import be.eurospacecenter.revise.notification.LobbyNotifier;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static be.eurospacecenter.revise.helper.LobbyCode.generateCode;

@Service
public class LobbyService implements Cleanable {

    protected final Map<String, Lobby> lobbies = new ConcurrentHashMap<>();
    private final SecureRandom random = new SecureRandom();

    private final MissionService missionService;
    private final LobbyNotifier notifier;

    public LobbyService(
            MissionService missionService,
            LobbyNotifier notifier
    ) {
        this.missionService = missionService;
        this.notifier = notifier;
    }

    public LobbyInfoResponse getLobbyInfo(String lobbyCode) {
        Lobby lobby = getLobby(lobbyCode);

        return new LobbyInfoResponse(lobby.getFreeTeamLabels(), lobby.getAllTeamLabels());
    }

    public LobbyCreationResponse createLobby(int numberOfTeams) {
        String lobbyCode = generateCode(random);
        UUID hostId = UUID.randomUUID();

        Lobby lobby = new Lobby(new Host(hostId), numberOfTeams, LocalDateTime.now());
        lobbies.put(lobbyCode, lobby);

        return new LobbyCreationResponse(lobbyCode, hostId.toString());
    }

    public LobbyJoinedResponse joinLobby(String lobbyCode) {
        Lobby lobby = getLobby(lobbyCode);
        UUID clientId = UUID.randomUUID();

        lobby.addTeam(clientId);

        notifier.notifyClientJoined(lobbyCode);

        return new LobbyJoinedResponse(clientId.toString(), lobby.getFreeTeamLabels(), lobby.getAllTeamLabels());
    }

    public void assignTeam(String lobbyCode, UUID clientId, String teamLabel) {
        Lobby lobby = getLobby(lobbyCode);

        lobby.assignTeam(clientId, teamLabel);

        notifier.notifyTeamJoined(lobbyCode, teamLabel);
    }

    public void startGame(String lobbyCode, UUID hostId) {
        Lobby lobby = getLobby(lobbyCode);

        lobby.startGame(hostId);

        missionService.registerManager(lobbyCode, lobby.getGameInfo());

        notifier.notifyGameStarted(lobbyCode);
    }

    private Lobby getLobby(String lobbyCode) {
        return Optional.ofNullable(lobbies.get(lobbyCode)).orElseThrow(() -> new NotFoundException(ErrorKeys.LOBBY_NOT_FOUND));
    }

    /** DO NOT USE, only for testing purposes */
    public void addLobby(String lobbyCode, Lobby lobby) {
        lobbies.put(lobbyCode, lobby);
    }

    @Override
    public void cleanUp(List<String> toRemove) {
        toRemove.forEach(lobbies::remove);
    }
}