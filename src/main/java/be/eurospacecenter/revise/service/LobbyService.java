package be.eurospacecenter.revise.service;

import be.eurospacecenter.revise.model.Host;
import be.eurospacecenter.revise.model.Lobby;
import be.eurospacecenter.revise.model.Team;
import be.eurospacecenter.revise.model.TeamId;
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
    private final LobbyNotifier notifier;

    public LobbyService(LobbyNotifier notifier) {
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

        notifier.notifyPlayerJoined(lobbyCode, teamLabel);
    }

    public void startGame(String lobbyCode, UUID hostId) {
        Lobby lobby = getLobby(lobbyCode);

        lobby.startGame(hostId);

        notifier.notifyGameStarted(lobbyCode);
    }

    private Lobby getLobby(String lobbyCode) {
        return Optional.ofNullable(lobbies.get(lobbyCode)).orElseThrow(() -> new IllegalArgumentException("Lobby introuvable"));
    }
}