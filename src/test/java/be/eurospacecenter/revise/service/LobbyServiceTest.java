package be.eurospacecenter.revise.service;

import be.eurospacecenter.revise.dto.LobbyEvent;
import be.eurospacecenter.revise.dto.LobbyEventType;
import be.eurospacecenter.revise.dto.TeamJoinedPayload;
import be.eurospacecenter.revise.exceptions.InvalidStartLobbyException;
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
        lobbyService.joinLobby(code, "INGE");

        ArgumentCaptor<LobbyEvent> eventCaptor = ArgumentCaptor.forClass(LobbyEvent.class);

        verify(messagingTemplate).convertAndSend(eq("/topic/lobby/" + code), eventCaptor.capture());

        LobbyEvent event = eventCaptor.getValue();
        assertThat(event.type()).isEqualTo(LobbyEventType.TEAM_JOINED);

        TeamJoinedPayload payload = (TeamJoinedPayload) event.payload();
        assertThat(payload.teamLabel()).isEqualTo("INGE");
    }

    @Test
    void shouldCreateLobbyWithUniqueCode() {
        String code1 = lobbyService.createLobby();
        String code2 = lobbyService.createLobby();

        assertThat(code1).isNotEqualTo(code2);
    }

    @Test
    void shouldAllowJoiningLobby() {
        String code = lobbyService.createLobby();
        lobbyService.joinLobby(code, "INGE");

        assertThat(lobbyService.lobbies.get(code).getTeams()).hasSize(1);
        assertThat(lobbyService.lobbies.get(code).getTeams().getFirst().label()).isEqualTo("INGE");
    }

    @Test
    void shouldNotAllowDuplicateTeamLabels() {
        String code = lobbyService.createLobby();
        lobbyService.joinLobby(code, "INGE");

        try {
            lobbyService.joinLobby(code, "INGE");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).isEqualTo("Cette équipe est déjà prise");
        }
    }

    @Test
    void shouldNotifyGameStarted() {
        String code = lobbyService.createLobby();

        lobbyService.joinLobby(code, "INGE");
        lobbyService.joinLobby(code, "MEDI");
        lobbyService.joinLobby(code, "COOP");
        lobbyService.joinLobby(code, "MECA");

        lobbyService.startGame(code, lobbyService.lobbies.get(code).getHostId());

        ArgumentCaptor<LobbyEvent> eventCaptor = ArgumentCaptor.forClass(LobbyEvent.class);

        verify(messagingTemplate, times(5)).convertAndSend(eq("/topic/lobby/" + code), eventCaptor.capture());

        LobbyEvent event = eventCaptor.getValue();
        assertThat(event.type()).isEqualTo(LobbyEventType.GAME_STARTED);
    }

    @Test
    void shouldNotAllowNonHostToStartGame() {
        String code = lobbyService.createLobby();
        try {
            lobbyService.startGame(code, UUID.randomUUID());
        } catch (InvalidStartLobbyException e) {
            assertThat(e.getMessage()).isEqualTo("Seul l'hôte peut démarrer la partie");
        }
    }

    @Test
    void shouldNotAllowJoiningNonExistentLobby() {
        lobbyService.createLobby();
        try {
            lobbyService.joinLobby("INVALID", "INGE");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).isEqualTo("Lobby introuvable");
        }
    }
}