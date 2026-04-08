package be.eurospacecenter.revise.notification;

import be.eurospacecenter.revise.event.LauncherEvent;
import be.eurospacecenter.revise.event.LauncherEventType;
import be.eurospacecenter.revise.model.lobbycode.LobbyCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class WebSocketLauncherNotifier implements LauncherNotifier {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketLauncherNotifier.class);
    private static final String LAUNCHER_TOPIC_PREFIX = "/topic/launcher/";
    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketLauncherNotifier(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public void notifyLauncherEnded(LobbyCode lobbyCode) {
        logger.info("Sending LAUNCHER_ENDED event for discovery: {}", lobbyCode.lobbyCode());
        LauncherEvent event = new LauncherEvent(LauncherEventType.LAUNCHER_ENDED, null);

        messagingTemplate.convertAndSend(LAUNCHER_TOPIC_PREFIX + lobbyCode.lobbyCode(), event);
        logger.info("LAUNCHER_ENDED event sent successfully");
    }

    @Override
    public void notifyGameEnded(LobbyCode lobbyCode) {
        logger.info("Sending LAUNCHER_GAME_ENDED event for discovery: {}", lobbyCode.lobbyCode());
        LauncherEvent event = new LauncherEvent(LauncherEventType.LAUNCHER_GAME_ENDED, null);

        messagingTemplate.convertAndSend(LAUNCHER_TOPIC_PREFIX + lobbyCode.lobbyCode(), event);
        logger.info("LAUNCHER_GAME_ENDED event sent successfully");
    }
}
