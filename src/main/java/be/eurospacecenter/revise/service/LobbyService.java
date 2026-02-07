package be.eurospacecenter.revise.service;

import be.eurospacecenter.revise.dto.LobbyEvent;
import be.eurospacecenter.revise.dto.LobbyEventType;
import be.eurospacecenter.revise.dto.TeamJoinedPayload;
import be.eurospacecenter.revise.model.Host;
import be.eurospacecenter.revise.model.Lobby;
import be.eurospacecenter.revise.model.Team;
import be.eurospacecenter.revise.model.TeamId;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static be.eurospacecenter.revise.helper.LobbyCode.generateCode;


@Service
public class LobbyService {

    private final Map<String, Lobby> lobbies = new ConcurrentHashMap<>();
    private final SecureRandom random = new SecureRandom();
    private final SimpMessagingTemplate messagingTemplate;

    public LobbyService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public String createLobby() {
        String code = generateCode(random);
        Host host = new Host(UUID.randomUUID());
        Lobby lobby = new Lobby(code, host);

        lobbies.put(code, lobby);
        return lobby.getCode();
    }

    public void joinLobby(String code, String teamLabel) {
        Lobby lobby = getLobby(code);

        lobby.teamLabelIsAvailable(teamLabel);

        Team team = new Team(TeamId.valueOf(teamLabel), UUID.randomUUID());
        lobby.addPlayer(team);

        notifyPlayerJoined(lobby, team);
    }

    public void startGame(String code, UUID hostId) {
        Lobby lobby = getLobby(code);

        if (!lobby.isHost(hostId)) {
            throw new IllegalArgumentException("Seul l'hôte peut démarrer la partie");
        }

        // Lancer la partie et avertie les joueurs
    }

    private Lobby getLobby(String code) {
        return Optional.ofNullable(lobbies.get(code)).orElseThrow(() -> new IllegalArgumentException("Lobby introuvable"));
    }

    private void notifyPlayerJoined(Lobby lobby, Team team) {
        LobbyEvent event = new LobbyEvent(LobbyEventType.TEAM_JOINED, new TeamJoinedPayload(team.label()));

        messagingTemplate.convertAndSend("/topic/lobby/" + lobby.getCode(), event);
    }
}