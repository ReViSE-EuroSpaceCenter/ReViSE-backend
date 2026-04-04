package be.eurospacecenter.revise.notification;

import be.eurospacecenter.revise.dto.event.ResourceEvent;
import be.eurospacecenter.revise.dto.event.ResourceEventType;
import be.eurospacecenter.revise.model.resource.TeamResources;
import be.eurospacecenter.revise.model.lobbycode.LobbyCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class WebSocketResourceNotifier implements ResourceNotifier {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketResourceNotifier.class);
    private static final String RESOURCE_TOPIC_PREFIX = "/topic/resource/";
    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketResourceNotifier(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public void notifyEncodingStarted(LobbyCode lobbyCode) {
        logger.info("Sending RESOURCE_STARTED event for discovery: {}", lobbyCode.lobbyCode());
        ResourceEvent event = new ResourceEvent(ResourceEventType.RESOURCE_STARTED, null);

        messagingTemplate.convertAndSend(RESOURCE_TOPIC_PREFIX + lobbyCode.lobbyCode(), event);
        logger.info("RESOURCE_STARTED event sent successfully");
    }

    @Override
    public void notifyResourcesUpdated(LobbyCode lobbyCode, TeamResources teamResources) {
        logger.info("Sending RESOURCE_UPDATED event for discovery: {}", lobbyCode.lobbyCode());
        ResourceEvent event = new ResourceEvent(ResourceEventType.RESOURCE_UPDATED, teamResources);

        messagingTemplate.convertAndSend(RESOURCE_TOPIC_PREFIX + lobbyCode.lobbyCode(), event);
        logger.info("RESOURCE_UPDATED event sent successfully");
    }

    @Override
    public void notifyEncodingEnded(LobbyCode lobbyCode) {
        logger.info("Sending RESOURCE_ENDED event for discovery: {}", lobbyCode.lobbyCode());
        ResourceEvent event = new ResourceEvent(ResourceEventType.RESOURCE_ENDED, null);

        messagingTemplate.convertAndSend(RESOURCE_TOPIC_PREFIX + lobbyCode.lobbyCode(), event);
        logger.info("RESOURCE_ENDED event sent successfully");
    }
}
