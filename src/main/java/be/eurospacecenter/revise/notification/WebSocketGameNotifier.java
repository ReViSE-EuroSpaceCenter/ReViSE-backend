package be.eurospacecenter.revise.notification;

import be.eurospacecenter.revise.dto.event.GameEvent;
import be.eurospacecenter.revise.dto.event.GameEventType;

import be.eurospacecenter.revise.dto.response.TeamMissionCompletedResponse;
import be.eurospacecenter.revise.model.MissionType;
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
    public void notifyTeamMissionCompleted(String lobbyCode, String teamLabel, MissionType missionType) {
        logger.info("Sending TEAM_MISSION_COMPLETED event for game: {} with team: {} and mission: {}", lobbyCode, teamLabel, missionType);
        GameEvent event = new GameEvent(GameEventType.TEAM_MISSION_COMPLETED, new TeamMissionCompletedResponse(teamLabel, missionType));

        messagingTemplate.convertAndSend(GAME_TOPIC_PREFIX + lobbyCode, event);
        logger.info("TEAM_MISSION_COMPLETED event sent successfully");

    }
}
