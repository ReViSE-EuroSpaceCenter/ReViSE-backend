package be.eurospacecenter.revise.notification;

import be.eurospacecenter.revise.dto.LobbyEvent;
import be.eurospacecenter.revise.dto.LobbyEventType;
import be.eurospacecenter.revise.dto.TeamJoinedPayload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class WebSocketLobbyNotifier implements LobbyNotifier {

    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketLobbyNotifier(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public void notifyTeamJoined(String lobbyCode, String teamLabel) {
        LobbyEvent event = new LobbyEvent(LobbyEventType.TEAM_JOINED, new TeamJoinedPayload(teamLabel));

        messagingTemplate.convertAndSend("/topic/lobby/" + lobbyCode, event);
    }

    @Override
    public void notifyGameStarted(String lobbyCode) {
        LobbyEvent event = new LobbyEvent(LobbyEventType.GAME_STARTED, null);

        messagingTemplate.convertAndSend("/topic/lobby/" + lobbyCode, event);
    }
}
