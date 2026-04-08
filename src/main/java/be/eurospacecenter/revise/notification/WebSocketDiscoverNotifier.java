package be.eurospacecenter.revise.notification;

import be.eurospacecenter.revise.event.DiscoverEvent;
import be.eurospacecenter.revise.event.DiscoverEventType;
import be.eurospacecenter.revise.model.lobbycode.LobbyCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class WebSocketDiscoverNotifier implements DiscoverNotifier {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketDiscoverNotifier.class);
    private static final String DISCOVER_TOPIC_PREFIX = "/topic/discover/";
    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketDiscoverNotifier(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public void notifyGameEnded(LobbyCode lobbyCode) {
        logger.info("Sending DISCOVER_GAME_ENDED event for discovery: {}", lobbyCode.lobbyCode());
        DiscoverEvent event = new DiscoverEvent(DiscoverEventType.DISCOVER_GAME_ENDED, null);

        messagingTemplate.convertAndSend(DISCOVER_TOPIC_PREFIX + lobbyCode.lobbyCode(), event);
        logger.info("DISCOVER_GAME_ENDED event sent successfully");
    }
}
