package be.eurospacecenter.revise.notification;

import be.eurospacecenter.revise.dto.event.LauncherEvent;
import be.eurospacecenter.revise.dto.event.LauncherEventType;
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
    public void notifyResourceUpdated(String lobbyCode, String resourceName) {
        logger.info("Sending RESOURCE_UPDATED event for launcher: {}", lobbyCode);
        LauncherEvent event = new LauncherEvent(LauncherEventType.RESOURCE_UPDATED, resourceName);

        messagingTemplate.convertAndSend(LAUNCHER_TOPIC_PREFIX + lobbyCode, event);
        logger.info("RESOURCE_UPDATED event sent successfully");
    }
}
