package be.eurospacecenter.revise.notification;

import be.eurospacecenter.revise.event.MissionEvent;
import be.eurospacecenter.revise.event.MissionEventType;

import be.eurospacecenter.revise.dto.team.TeamProgressionDTO;
import be.eurospacecenter.revise.model.lobbycode.LobbyCode;
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
    public void notifyTeamProgression(LobbyCode lobbyCode, TeamProgression teamProgression) {
        logger.info("Sending TEAM_PROGRESSION event for game: {} with team: {} and progression: {}", lobbyCode.lobbyCode(), teamProgression.teamLabel(), teamProgression);
        MissionEvent event = new MissionEvent(MissionEventType.TEAM_PROGRESSION, TeamProgressionDTO.fromTeamProgression(teamProgression));

        messagingTemplate.convertAndSend(GAME_TOPIC_PREFIX + lobbyCode.lobbyCode(), event);
        logger.info("TEAM_PROGRESSION event sent successfully");
    }

    @Override
    public void notifyMissionEnded(LobbyCode lobbyCode) {
        logger.info("Sending MISSION_ENDED event for game: {}", lobbyCode.lobbyCode());
        MissionEvent event = new MissionEvent(MissionEventType.MISSION_ENDED, null);

        messagingTemplate.convertAndSend(GAME_TOPIC_PREFIX + lobbyCode.lobbyCode(), event);
        logger.info("MISSION_ENDED event sent successfully");
    }
}
