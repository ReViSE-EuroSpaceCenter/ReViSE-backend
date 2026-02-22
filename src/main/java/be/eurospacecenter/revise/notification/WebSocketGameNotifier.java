package be.eurospacecenter.revise.notification;

import be.eurospacecenter.revise.dto.event.GameEvent;
import be.eurospacecenter.revise.dto.event.GameEventType;

import be.eurospacecenter.revise.dto.response.TeamProgressionResponse;
import be.eurospacecenter.revise.model.TeamProgression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class WebSocketGameNotifier implements GameNotifier {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketGameNotifier.class);
    private static final String GAME_TOPIC_PREFIX = "/topic/game/";
    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketGameNotifier(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public void notifyTeamProgression(String lobbyCode, String teamLabel, TeamProgression teamProgression) {
        logger.info("Sending TEAM_PROGRESSION event for game: {} with team: {} and progression: {}", lobbyCode, teamLabel, teamProgression);
        GameEvent event = new GameEvent(GameEventType.TEAM_PROGRESSION, new TeamProgressionResponse(teamLabel, teamProgression));

        messagingTemplate.convertAndSend(GAME_TOPIC_PREFIX + lobbyCode, event);
        logger.info("TEAM_PROGRESSION event sent successfully");

    }
}
