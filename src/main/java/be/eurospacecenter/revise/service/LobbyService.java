package be.eurospacecenter.revise.service;

import be.eurospacecenter.revise.dto.response.LobbyCreationResponse;
import be.eurospacecenter.revise.dto.response.LobbyInfoResponse;
import be.eurospacecenter.revise.dto.response.LobbyJoinedResponse;
import be.eurospacecenter.revise.exceptions.NoAutoriseOperationException;
import be.eurospacecenter.revise.exceptions.NotFoundException;
import be.eurospacecenter.revise.model.*;
import be.eurospacecenter.revise.notification.LobbyNotifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static be.eurospacecenter.revise.helper.LobbyCode.generateCode;

@Service
public class LobbyService {

    private static final Logger logger = LoggerFactory.getLogger(LobbyService.class);

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

        Lobby lobby = new Lobby(new Host(hostId), numberOfTeams, LocalDateTime.now());
        lobbies.put(lobbyCode, lobby);

        return new LobbyCreationResponse(lobbyCode, hostId.toString());
    }

    public LobbyInfoResponse getLobbyInfo(String lobbyCode) {
        Lobby lobby = getLobby(lobbyCode);

        return new LobbyInfoResponse(lobby.getFreeTeamLabels(), lobby.getAllTeamLabels());
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
        ensureClient(lobby, clientId);

        lobby.assignTeam(clientId, teamLabel);

        notifier.notifyTeamJoined(lobbyCode, teamLabel);
    }

    public void startGame(String lobbyCode, UUID hostId) {
        Lobby lobby = getLobby(lobbyCode);
        ensureHost(lobby, hostId);

        lobby.startGame(hostId);

        Game game = new Game(lobby.getTeams());
        gameService.registerGame(lobbyCode, game);

        notifier.notifyGameStarted(lobbyCode);
    }

    protected Lobby getLobby(String lobbyCode) {
        return Optional.ofNullable(lobbies.get(lobbyCode)).orElseThrow(() -> new NotFoundException("Lobby introuvable"));
    }

    protected void ensureClient(Lobby lobby, UUID clientId) {
        if (!lobby.isClient(clientId)) {
            throw new NoAutoriseOperationException("Client introuvable dans le lobby");
        }
    }

    protected void ensureHost(Lobby lobby, UUID hostId) {
        if (lobby.isNotHost(hostId)) {
            throw new NoAutoriseOperationException("Action réservée à l'hôte du lobby");
        }
    }

    @Scheduled(cron = "0 0 */12 * * *")
    protected void clearLobbies() {
        List<String> toRemove = new ArrayList<>();

        lobbies.forEach((code, lobby) -> {
            if (LocalDateTime.now().isAfter(lobby.getExpiresAt())) {
                toRemove.add(code);
            }
        });

        toRemove.forEach(lobbies::remove);
        gameService.clearGames(toRemove);

        logger.info("Clearing {} games", toRemove.size());
    }

    // DO NOT USE, only for testing purposes
    public void addLobby(String lobbyCode, Lobby lobby) {
        lobbies.put(lobbyCode, lobby);
    }

}