package be.eurospacecenter.revise.service;

import be.eurospacecenter.revise.exceptions.ErrorKeys;
import be.eurospacecenter.revise.exceptions.NoAutoriseOperationException;
import be.eurospacecenter.revise.exceptions.NotFoundException;
import be.eurospacecenter.revise.model.lobby.LobbyCreation;
import be.eurospacecenter.revise.model.lobby.LobbyJoined;
import be.eurospacecenter.revise.model.lobby.TeamLabel;
import be.eurospacecenter.revise.model.lobbycode.LobbyCode;
import be.eurospacecenter.revise.model.lobbycode.LobbyCodeGenerator;
import be.eurospacecenter.revise.notification.LobbyNotifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class LobbyServiceTest {

    private LobbyService lobbyService;
    @Autowired private LobbyCodeGenerator lobbyCodeGenerator;
    private final MissionService missionService = mock(MissionService.class);
    private final LobbyNotifier notifier = mock(LobbyNotifier.class);

    // ---------------------------------------------------------------------
    // Setup
    // ---------------------------------------------------------------------

    @BeforeEach
    void setup() {
        lobbyService = new LobbyService(missionService, notifier, lobbyCodeGenerator);
    }

    // ---------------------------------------------------------------------
    // Tests
    // ---------------------------------------------------------------------

    @ParameterizedTest
    @ValueSource(ints = {4, 6})
    void lobbyCreationAndJoinShouldWork(int teamCount) {
        LobbyCreation creation = lobbyService.createLobby(teamCount);
        assertNotNull(creation.hostId());
        assertEquals(6, creation.lobbyCode().lobbyCode().length());

        LobbyJoined join = lobbyService.joinLobby(creation.lobbyCode());
        assertEquals(teamCount, join.availableTeams().size());
        assertEquals(teamCount, join.allTeams().size());
    }

    @ParameterizedTest
    @ValueSource(ints = {4, 6})
    void availableTeamsShouldDecreaseAtEachAssign(int teamCount) {
        LobbyCode code = lobbyService.createLobby(teamCount).lobbyCode();

        for (int i = teamCount; i > 0; i--) {
            LobbyJoined res = lobbyService.joinLobby(code);
            assertEquals(i, res.availableTeams().size());

            TeamLabel team = res.availableTeams().iterator().next();
            lobbyService.assignTeam(code, UUID.fromString(res.clientId()), team);
        }
    }

    @Test
    void teamAssignmentRules() {
        LobbyCreation lobby = lobbyService.createLobby(4);
        LobbyCode code = lobby.lobbyCode();

        LobbyJoined client1 = lobbyService.joinLobby(code);
        UUID id1 = UUID.fromString(client1.clientId());
        TeamLabel label = TeamLabel.EXPE;

        lobbyService.assignTeam(code, id1, label);

        LobbyJoined client2 = lobbyService.joinLobby(code);
        assertError(IllegalArgumentException.class, ErrorKeys.TEAM_LABEL_ALREADY_TAKEN,
                () -> lobbyService.assignTeam(code, UUID.fromString(client2.clientId()), label));

        assertError(IllegalArgumentException.class, ErrorKeys.CLIENT_ALREADY_CHOSE_TEAM,
                () -> lobbyService.assignTeam(code, id1, TeamLabel.AERO));
    }

    @Test
    void startLobbySecurity() {
        LobbyCreation lobby = lobbyService.createLobby(4);
        LobbyCode code = lobby.lobbyCode();

        assertError(NoAutoriseOperationException.class, ErrorKeys.ACTION_RESERVED_TO_HOST,
                () -> lobbyService.startGame(code, UUID.randomUUID()));

        assertError(NotFoundException.class, ErrorKeys.LOBBY_NOT_FOUND,
                () -> lobbyService.startGame(new LobbyCode("XXXXXX"), lobby.hostId()));
    }

    @ParameterizedTest
    @ValueSource(ints = {4, 6})
    void fullLobbyCycleShouldSucceed(int teamCount) {
        LobbyCreation lobby = lobbyService.createLobby(teamCount);
        LobbyCode code = lobby.lobbyCode();

        for (TeamLabel label : lobbyService.getLobbyInfo(code).getAllTeamLabels()) {
            LobbyJoined res = lobbyService.joinLobby(code);
            lobbyService.assignTeam(code, UUID.fromString(res.clientId()), label);
        }

        assertDoesNotThrow(() -> lobbyService.startGame(code, lobby.hostId()));
    }

    // ---------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------

    private void assertError(Class<? extends Exception> type, String key, Runnable action) {
        Exception ex = assertThrows(type, action::run);
        assertEquals(key, ex.getMessage());
    }
}
