package be.eurospacecenter.revise.notification;

import be.eurospacecenter.revise.dto.event.LobbyEvent;
import be.eurospacecenter.revise.dto.event.LobbyEventType;
import be.eurospacecenter.revise.dto.response.TeamJoinedDTO;
import be.eurospacecenter.revise.model.lobby.TeamLabel;
import be.eurospacecenter.revise.model.lobbycode.LobbyCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class WebSocketLobbyNotifier implements LobbyNotifier {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketLobbyNotifier.class);
    private static final String LOBBY_TOPIC_PREFIX = "/topic/lobby/";
    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketLobbyNotifier(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public void notifyTeamJoined(LobbyCode lobbyCode, TeamLabel teamLabel) {
        logger.info("Sending TEAM_JOINED event for lobby: {} with team: {}", lobbyCode.lobbyCode(), teamLabel);
        LobbyEvent event = new LobbyEvent(LobbyEventType.TEAM_JOINED, new TeamJoinedDTO(teamLabel));

        messagingTemplate.convertAndSend(LOBBY_TOPIC_PREFIX + lobbyCode.lobbyCode(), event);
        logger.info("TEAM_JOINED event sent successfully");
    }

    @Override
    public void notifyClientJoined(LobbyCode lobbyCode) {
        logger.info("Sending CLIENT_JOINED event for lobby: {}", lobbyCode.lobbyCode());
        LobbyEvent event = new LobbyEvent(LobbyEventType.CLIENT_JOINED, null);

        messagingTemplate.convertAndSend(LOBBY_TOPIC_PREFIX + lobbyCode.lobbyCode(), event);
        logger.info("CLIENT_JOINED event sent successfully");
    }

    @Override
    public void notifyGameStarted(LobbyCode lobbyCode) {
        logger.info("Sending GAME_STARTED event for lobby: {}", lobbyCode.lobbyCode());
        LobbyEvent event = new LobbyEvent(LobbyEventType.GAME_STARTED, null);

        messagingTemplate.convertAndSend(LOBBY_TOPIC_PREFIX + lobbyCode.lobbyCode(), event);
        logger.info("GAME_STARTED event sent successfully");
    }
}
