package be.eurospacecenter.revise.notification;

import be.eurospacecenter.revise.dto.LobbyEvent;
import be.eurospacecenter.revise.dto.LobbyEventType;
import be.eurospacecenter.revise.dto.TeamJoinedPayload;
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
    public void notifyTeamJoined(String lobbyCode, String teamLabel) {
        logger.info("Sending TEAM_JOINED event for lobby: {} with team: {}", lobbyCode, teamLabel);
        LobbyEvent event = new LobbyEvent(LobbyEventType.TEAM_JOINED, new TeamJoinedPayload(teamLabel));

        messagingTemplate.convertAndSend(LOBBY_TOPIC_PREFIX + lobbyCode, event);
        logger.info("TEAM_JOINED event sent successfully");
    }

    @Override
    public void notifyGameStarted(String lobbyCode) {
        logger.info("Sending GAME_STARTED event for lobby: {}", lobbyCode);
        LobbyEvent event = new LobbyEvent(LobbyEventType.GAME_STARTED, null);

        messagingTemplate.convertAndSend(LOBBY_TOPIC_PREFIX + lobbyCode, event);
        logger.info("GAME_STARTED event sent successfully");
    }
}
