package be.eurospacecenter.revise.service;

import be.eurospacecenter.revise.dto.response.LobbyCreationResponse;
import be.eurospacecenter.revise.dto.response.LobbyInfoResponse;
import be.eurospacecenter.revise.dto.response.LobbyJoinedResponse;
import be.eurospacecenter.revise.exceptions.ErrorKeys;
import be.eurospacecenter.revise.exceptions.NoAutoriseOperationException;
import be.eurospacecenter.revise.exceptions.NotFoundException;
import be.eurospacecenter.revise.model.lobby.TeamLabel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
class LobbyServiceTest {

    @Autowired
    private LobbyService lobbyService;

    private UUID hostIdFor4;
    private String lobbyCodeFor4;

    private UUID hostIdFor6;
    private String lobbyCodeFor6;

    @BeforeEach
    void setup() {
        lobbyService.cleanUp(lobbyService.lobbies.keySet().stream().toList());

        LobbyCreationResponse res4 = lobbyService.createLobby(4);
        hostIdFor4 = UUID.fromString(res4.hostId());
        lobbyCodeFor4 = res4.lobbyCode();

        LobbyCreationResponse res6 = lobbyService.createLobby(6);
        hostIdFor6 = UUID.fromString(res6.hostId());
        lobbyCodeFor6 = res6.lobbyCode();
    }

    @Test
    void lobbyShouldReturnLobbyCodeFor4Teams() {
        LobbyCreationResponse res = lobbyService.createLobby(4);

        UUID hostId = UUID.fromString(res.hostId());

        assertNotNull(hostId);
        assertFalse(res.lobbyCode().isEmpty());
        assertEquals(6, res.lobbyCode().length());
    }

    @Test
    void lobbyShouldReturnLobbyCodeFor6Teams() {
        LobbyCreationResponse res = lobbyService.createLobby(6);

        UUID hostId = UUID.fromString(res.hostId());

        assertNotNull(hostId);
        assertFalse(res.lobbyCode().isEmpty());
        assertEquals(6, res.lobbyCode().length());
    }

    @Test
    void joinLobbyShouldSucceedFor4Teams() {
        LobbyJoinedResponse res = lobbyService.joinLobby(lobbyCodeFor4);

        UUID clientId = UUID.fromString(res.clientId());

        assertNotNull(clientId);
        assertEquals(4, res.availableTeams().size());
        assertEquals(4, res.allTeams().size());
    }


    @Test
    void joinLobbyShouldSucceedFor6Teams() {
        LobbyJoinedResponse res = lobbyService.joinLobby(lobbyCodeFor6);

        UUID clientId = UUID.fromString(res.clientId());

        assertNotNull(clientId);
        assertEquals(6, res.availableTeams().size());
        assertEquals(6, res.allTeams().size());
    }

    @Test
    void availableTeamsShouldDecreaseAtEachAssignFor4() {
        for (int i = 4; i > 0; i--) {
            LobbyJoinedResponse res = lobbyService.joinLobby(lobbyCodeFor4);

            UUID clientId = UUID.fromString(res.clientId());

            assertEquals(4, res.allTeams().size());
            assertEquals(i, res.availableTeams().size());

            lobbyService.assignTeam(lobbyCodeFor4, clientId, res.availableTeams().get(i - 1));
        }
    }

    @Test
    void availableTeamsShouldDecreaseAtEachAssignFor6() {
        for (int i = 6; i > 0; i--) {
            LobbyJoinedResponse res = lobbyService.joinLobby(lobbyCodeFor6);

            UUID clientId = UUID.fromString(res.clientId());

            assertEquals(6, res.allTeams().size());
            assertEquals(i, res.availableTeams().size());

            lobbyService.assignTeam(lobbyCodeFor6, clientId, res.availableTeams().get(i - 1));
        }
    }

    @Test
    void joiningUnknownLobbyCodeShouldFail() {
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> lobbyService.joinLobby("INVALID")
        );
        assertEquals(ErrorKeys.LOBBY_NOT_FOUND, ex.getMessage());
    }

    @Test
    void teamLabelAlreadyAssignShouldFailFor4() {
        Set<TeamLabel> teamLabelSet = TeamLabel.getAllowedLabels(true);

        teamLabelSet.forEach(l -> {
            LobbyJoinedResponse firstTeam = lobbyService.joinLobby(lobbyCodeFor4);
            UUID firstId = UUID.fromString(firstTeam.clientId());
            String label = l.toString();

            lobbyService.assignTeam(lobbyCodeFor4, firstId, label);

            LobbyJoinedResponse secondTeam = lobbyService.joinLobby(lobbyCodeFor4);
            UUID secondId = UUID.fromString(secondTeam.clientId());

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> lobbyService.assignTeam(lobbyCodeFor4, secondId, label)
            );
            assertEquals(ErrorKeys.TEAM_LABEL_ALREADY_TAKEN, ex.getMessage());
        });
    }

    @Test
    void teamLabelAlreadyAssignShouldFailFor6() {
        Set<TeamLabel> teamLabelSet = TeamLabel.getAllowedLabels(false);

        teamLabelSet.forEach(l -> {
            LobbyJoinedResponse firstTeam = lobbyService.joinLobby(lobbyCodeFor6);
            UUID firstId = UUID.fromString(firstTeam.clientId());

            String label = l.toString();

            lobbyService.assignTeam(lobbyCodeFor6, firstId, label);

            LobbyJoinedResponse secondTeam = lobbyService.joinLobby(lobbyCodeFor6);
            UUID secondId = UUID.fromString(secondTeam.clientId());

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> lobbyService.assignTeam(lobbyCodeFor6, secondId, label)
            );
            assertEquals(ErrorKeys.TEAM_LABEL_ALREADY_TAKEN, ex.getMessage());
        });
    }


    @Test
    void teamAssignTwiceShouldFailFor4() {
        Set<TeamLabel> teamLabelSet = TeamLabel.getAllowedLabels(true);

        teamLabelSet.forEach(l -> {
            LobbyJoinedResponse firstTeam = lobbyService.joinLobby(lobbyCodeFor4);
            UUID firstId = UUID.fromString(firstTeam.clientId());
            String label = l.toString();

            lobbyService.assignTeam(lobbyCodeFor4, firstId, label);

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> lobbyService.assignTeam(lobbyCodeFor4, firstId, label)
            );
            assertEquals(ErrorKeys.CLIENT_ALREADY_CHOSE_TEAM, ex.getMessage());
        });
    }

    @Test
    void startingLobbyShouldSucceedFor4() {
        Set<TeamLabel> teamLabelSet = TeamLabel.getAllowedLabels(true);

        teamLabelSet.forEach(l -> {
            LobbyJoinedResponse team = lobbyService.joinLobby(lobbyCodeFor4);
            UUID teamId = UUID.fromString(team.clientId());

            lobbyService.assignTeam(lobbyCodeFor4, teamId, l.toString());
        });

        assertDoesNotThrow(() -> lobbyService.startGame(lobbyCodeFor4, hostIdFor4));
    }

    @Test
    void startingLobbyShouldSucceedFor6() {
        Set<TeamLabel> teamLabelSet = TeamLabel.getAllowedLabels(false);

        teamLabelSet.forEach(l -> {
            LobbyJoinedResponse team = lobbyService.joinLobby(lobbyCodeFor6);
            UUID teamId = UUID.fromString(team.clientId());

            lobbyService.assignTeam(lobbyCodeFor6, teamId, l.toString());
        });

        assertDoesNotThrow(() -> lobbyService.startGame(lobbyCodeFor6, hostIdFor6));
    }

    @Test
    void startingUnknownLobbyShouldFail() {
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> lobbyService.startGame("INVALID", hostIdFor4)
        );
        assertEquals(ErrorKeys.LOBBY_NOT_FOUND, ex.getMessage());
    }

    @Test
    void startingLobbyWithWrongHostIdShouldFailFor4() {
        UUID unknowId = UUID.randomUUID();

        assertNotEquals(unknowId, hostIdFor4);
        NoAutoriseOperationException ex = assertThrows(NoAutoriseOperationException.class,
                () -> lobbyService.startGame(lobbyCodeFor4, unknowId)
        );
        assertEquals(ErrorKeys.ACTION_RESERVED_TO_HOST, ex.getMessage());
    }

    @Test
    void startingLobbyWithWrongHostIdShouldFailFor6() {
        UUID unknowId = UUID.randomUUID();

        assertNotEquals(unknowId, hostIdFor6);
        NoAutoriseOperationException ex = assertThrows(NoAutoriseOperationException.class,
                () -> lobbyService.startGame(lobbyCodeFor6, unknowId)
        );
        assertEquals(ErrorKeys.ACTION_RESERVED_TO_HOST, ex.getMessage());
    }

    @Test
    void getLobbyInfoShouldFailForUnknown() {
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> lobbyService.getLobbyInfo("INVALID")
        );
        assertEquals(ErrorKeys.LOBBY_NOT_FOUND, ex.getMessage());
    }

    @Test
    void getLobbyInfoForFour() {
        LobbyInfoResponse res = lobbyService.getLobbyInfo(lobbyCodeFor4);

        assertEquals(4, res.allTeams().size());
        assertEquals(4, res.availableTeams().size());
    }

    @Test
    void getLobbyInfoForSix() {
        LobbyInfoResponse res = lobbyService.getLobbyInfo(lobbyCodeFor6);

        assertEquals(6, res.allTeams().size());
        assertEquals(6, res.availableTeams().size());
    }

    @Test
    void getLobbyInfoShouldDecreaseAtEachJoinedFor4() {
        for (int i = 4; i > 0; i--) {
            LobbyJoinedResponse joinedResponse = lobbyService.joinLobby(lobbyCodeFor4);

            UUID clientId = UUID.fromString(joinedResponse.clientId());

            LobbyInfoResponse res = lobbyService.getLobbyInfo(lobbyCodeFor4);

            assertEquals(4, res.allTeams().size());
            assertEquals(i, res.availableTeams().size());

            lobbyService.assignTeam(lobbyCodeFor4, clientId, res.availableTeams().get(i - 1));
        }
    }

    @Test
    void getLobbyInfoShouldDecreaseAtEachJoinedFor6() {
        for (int i = 6; i > 0; i--) {
            LobbyJoinedResponse joinedResponse = lobbyService.joinLobby(lobbyCodeFor6);

            UUID clientId = UUID.fromString(joinedResponse.clientId());

            LobbyInfoResponse res = lobbyService.getLobbyInfo(lobbyCodeFor6);

            assertEquals(6, res.allTeams().size());
            assertEquals(i, res.availableTeams().size());

            lobbyService.assignTeam(lobbyCodeFor6, clientId, res.availableTeams().get(i - 1));
        }
    }
}
