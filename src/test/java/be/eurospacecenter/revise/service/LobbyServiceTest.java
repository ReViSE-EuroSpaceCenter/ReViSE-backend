package be.eurospacecenter.revise.service;

import be.eurospacecenter.revise.dto.LobbyEvent;
import be.eurospacecenter.revise.dto.LobbyEventType;
import be.eurospacecenter.revise.dto.TeamJoinedPayload;
import be.eurospacecenter.revise.notification.WebSocketLobbyNotifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class LobbyServiceTest {

    private final SimpMessagingTemplate messagingTemplate = mock(SimpMessagingTemplate.class);

    private final LobbyService lobbyService = new LobbyService(new WebSocketLobbyNotifier(messagingTemplate));

    @BeforeEach
    void setUp() {
        lobbyService.lobbies.clear();
    }

    @Test
    void shouldNotifyTeamsWhenTeamJoins() {
        String code = lobbyService.createLobby();
        lobbyService.joinLobby(code, "ING");

        ArgumentCaptor<LobbyEvent> eventCaptor = ArgumentCaptor.forClass(LobbyEvent.class);

        verify(messagingTemplate).convertAndSend(eq("/topic/lobby/" + code), eventCaptor.capture());

        LobbyEvent event = eventCaptor.getValue();
        assertThat(event.type()).isEqualTo(LobbyEventType.TEAM_JOINED);

        TeamJoinedPayload payload = (TeamJoinedPayload) event.payload();
        assertThat(payload.teamLabel()).isEqualTo("ING");
    }

    @Test
    void shouldNotAllowDuplicateTeamLabels() {
        String code = lobbyService.createLobby();
        lobbyService.joinLobby(code, "ING");

        try {
            lobbyService.joinLobby(code, "ING");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).isEqualTo("Cette équipe est déjà prise");
        }
    }

    @Test
    void shouldNotifyGameStarted() {
        String code = lobbyService.createLobby();
        lobbyService.startGame(code, lobbyService.lobbies.get(code).getHostId());

        ArgumentCaptor<LobbyEvent> eventCaptor = ArgumentCaptor.forClass(LobbyEvent.class);

        verify(messagingTemplate).convertAndSend(eq("/topic/lobby/" + code), eventCaptor.capture());

        LobbyEvent event = eventCaptor.getValue();
        assertThat(event.type()).isEqualTo(LobbyEventType.GAME_STARTED);
    }

    @Test
    void shouldNotAllowNonHostToStartGame() {
        String code = lobbyService.createLobby();
        try {
            lobbyService.startGame(code, UUID.randomUUID());
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).isEqualTo("Seul l'hôte peut démarrer la partie");
        }
    }
}