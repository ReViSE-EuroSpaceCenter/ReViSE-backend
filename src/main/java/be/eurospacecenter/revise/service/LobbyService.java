package be.eurospacecenter.revise.service;

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

    public String createLobby() {
        String lobbyCode = generateCode(random);
        Host host = new Host(UUID.randomUUID());
        Lobby lobby = new Lobby(lobbyCode, host);

        lobbies.put(lobbyCode, lobby);
        return lobby.getCode();
    }

    public void joinLobby(String lobbyCode, String teamLabel) {
        Lobby lobby = getLobby(lobbyCode);

        Team team = new Team(TeamId.valueOf(teamLabel), UUID.randomUUID());
        lobby.addTeam(team);

        notifier.notifyTeamJoined(lobbyCode, teamLabel);
    }

    public void startGame(String lobbyCode, UUID hostId) {
        Lobby lobby = getLobby(lobbyCode);

        lobby.validateLobby(hostId);

        Game game = new Game(lobby.getHost(), lobby.getTeams());
        gameService.registerGame(lobbyCode, game);

        notifier.notifyGameStarted(lobbyCode);
    }

    public Lobby getLobby(String lobbyCode) {
        return Optional.ofNullable(lobbies.get(lobbyCode)).orElseThrow(() -> new IllegalArgumentException("Lobby introuvable"));
    }
}