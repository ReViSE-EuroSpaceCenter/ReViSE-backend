package be.eurospacecenter.revise.service;

import be.eurospacecenter.revise.dto.response.LobbyCreationResponse;
import be.eurospacecenter.revise.dto.response.LobbyJoinedResponse;
import be.eurospacecenter.revise.exceptions.NoAutoriseOperationException;
import be.eurospacecenter.revise.exceptions.NotFoundException;
import be.eurospacecenter.revise.model.*;
import be.eurospacecenter.revise.notification.LobbyNotifier;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static be.eurospacecenter.revise.helper.LobbyCode.generateCode;


@Service
public class LobbyService {

    protected final Map<String, Lobby> lobbies = new ConcurrentHashMap<>();
    private final SecureRandom random = new SecureRandom();
    private final GameService gameService;
    private final LobbyNotifier notifier;

    public LobbyService(
            GameService gameService,
            LobbyNotifier notifier
    ) {
        this.gameService = gameService;
        this.notifier = notifier;
    }

    public LobbyCreationResponse createLobby(int numberOfTeams) {
        String lobbyCode = generateCode(random);
        UUID hostId = UUID.randomUUID();

        Lobby lobby = new Lobby(new Host(hostId), numberOfTeams);
        lobbies.put(lobbyCode, lobby);

        return new LobbyCreationResponse(lobbyCode, hostId.toString());
    }

    public LobbyJoinedResponse joinLobby(String lobbyCode) {
        Lobby lobby = getLobby(lobbyCode);
        UUID clientId = UUID.randomUUID();

        lobby.addTeam(new Team(clientId));
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

        Game game = new Game(lobby.getTeams());
        gameService.registerGame(lobbyCode, game);

        notifier.notifyGameStarted(lobbyCode);
    }

    public void ensureClient(String lobbyCode, UUID clientId) {
        Lobby lobby = getLobby(lobbyCode);
        if (!lobby.isInLobby(clientId)) {
            throw new NoAutoriseOperationException("Client introuvable dans le lobby");
        }
    }

    public Lobby getLobby(String lobbyCode) {
        return Optional.ofNullable(lobbies.get(lobbyCode)).orElseThrow(() -> new NotFoundException("Lobby introuvable"));
    }

    public void ensureHost(String lobbyCode, UUID hostId) {
        Lobby lobby = getLobby(lobbyCode);
        if (!lobby.isHost(hostId)) {
            throw new NoAutoriseOperationException("Action réservée à l'hôte du lobby");
        }
    }
}