package be.eurospacecenter.revise.notification;

import be.eurospacecenter.revise.dto.event.MissionEvent;
import be.eurospacecenter.revise.dto.event.MissionEventType;

import be.eurospacecenter.revise.dto.response.TeamProgressionResponse;
import be.eurospacecenter.revise.model.mission.TeamProgression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class WebSocketMissionNotifier implements MissionNotifier {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketMissionNotifier.class);
    private static final String GAME_TOPIC_PREFIX = "/topic/mission/";
    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketMissionNotifier(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public void notifyTeamProgression(String lobbyCode, String teamLabel, TeamProgression teamProgression) {
        logger.info("Sending TEAM_PROGRESSION event for game: {} with team: {} and progression: {}", lobbyCode, teamLabel, teamProgression);
        MissionEvent event = new MissionEvent(MissionEventType.TEAM_PROGRESSION, new TeamProgressionResponse(teamLabel, teamProgression));

        messagingTemplate.convertAndSend(GAME_TOPIC_PREFIX + lobbyCode, event);
        logger.info("TEAM_PROGRESSION event sent successfully");
    }

    @Override
    public void notifyMissionEnded(String lobbyCode) {
        logger.info("Sending MISSION_ENDED event for game: {}", lobbyCode);
        MissionEvent event = new MissionEvent(MissionEventType.MISSION_ENDED, null);

        messagingTemplate.convertAndSend(GAME_TOPIC_PREFIX + lobbyCode, event);
        logger.info("MISSION_ENDED event sent successfully");
    }
}
