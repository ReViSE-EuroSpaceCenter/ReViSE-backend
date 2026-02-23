package be.eurospacecenter.revise.service;

import be.eurospacecenter.revise.dto.response.LobbyCreationResponse;
import be.eurospacecenter.revise.dto.response.LobbyInfoResponse;
import be.eurospacecenter.revise.dto.response.LobbyJoinedResponse;
import be.eurospacecenter.revise.exceptions.NoAutoriseOperationException;
import be.eurospacecenter.revise.exceptions.NotFoundException;
import be.eurospacecenter.revise.model.Host;
import be.eurospacecenter.revise.model.Lobby;
import be.eurospacecenter.revise.model.TeamLabel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

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
        lobbyService.clearLobbies();

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

        Assertions.assertNotNull(hostId);
        Assertions.assertFalse(res.lobbyCode().isEmpty());
        Assertions.assertEquals(6, res.lobbyCode().length());
    }

    @Test
    void lobbyShouldReturnLobbyCodeFor6Teams() {
        LobbyCreationResponse res = lobbyService.createLobby(6);

        UUID hostId = UUID.fromString(res.hostId());

        Assertions.assertNotNull(hostId);
        Assertions.assertFalse(res.lobbyCode().isEmpty());
        Assertions.assertEquals(6, res.lobbyCode().length());
    }

    @Test
    void joinLobbyShouldSucceedFor4Teams() {
        LobbyJoinedResponse res = lobbyService.joinLobby(lobbyCodeFor4);

        UUID clientId = UUID.fromString(res.clientId());

        Assertions.assertNotNull(clientId);
        Assertions.assertEquals(4, res.availableTeams().size());
        Assertions.assertEquals(4, res.allTeams().size());
    }


    @Test
    void joinLobbyShouldSucceedFor6Teams() {
        LobbyJoinedResponse res = lobbyService.joinLobby(lobbyCodeFor6);

        UUID clientId = UUID.fromString(res.clientId());

        Assertions.assertNotNull(clientId);
        Assertions.assertEquals(6, res.availableTeams().size());
        Assertions.assertEquals(6, res.allTeams().size());
    }

    @Test
    void availableTeamsShouldDecreaseAtEachAssignFor4() {
        for (int i = 4; i > 0; i--) {
            LobbyJoinedResponse res = lobbyService.joinLobby(lobbyCodeFor4);

            UUID clientId = UUID.fromString(res.clientId());

            Assertions.assertEquals(4, res.allTeams().size());
            Assertions.assertEquals(i, res.availableTeams().size());

            lobbyService.assignTeam(lobbyCodeFor4, clientId, res.availableTeams().get(i - 1));
        }
    }

    @Test
    void availableTeamsShouldDecreaseAtEachAssignFor6() {
        for (int i = 6; i > 0; i--) {
            LobbyJoinedResponse res = lobbyService.joinLobby(lobbyCodeFor6);

            UUID clientId = UUID.fromString(res.clientId());

            Assertions.assertEquals(6, res.allTeams().size());
            Assertions.assertEquals(i, res.availableTeams().size());

            lobbyService.assignTeam(lobbyCodeFor6, clientId, res.availableTeams().get(i - 1));
        }
    }

    @Test
    void joiningUnknownLobbyCodeShouldFail() {
        Assertions.assertThrows(NotFoundException.class, () -> lobbyService.joinLobby("INVALID"));
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

            Assertions.assertThrows(IllegalArgumentException.class, () -> lobbyService.assignTeam(lobbyCodeFor4, secondId, label));
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

            Assertions.assertThrows(IllegalArgumentException.class, () -> lobbyService.assignTeam(lobbyCodeFor6, secondId, label));
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

            Assertions.assertThrows(IllegalArgumentException.class, () -> lobbyService.assignTeam(lobbyCodeFor4, firstId, label));
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

        Assertions.assertDoesNotThrow(() -> lobbyService.startGame(lobbyCodeFor4, hostIdFor4));
    }

    @Test
    void startingLobbyShouldSucceedFor6() {
        Set<TeamLabel> teamLabelSet = TeamLabel.getAllowedLabels(false);

        teamLabelSet.forEach(l -> {
            LobbyJoinedResponse team = lobbyService.joinLobby(lobbyCodeFor6);
            UUID teamId = UUID.fromString(team.clientId());

            lobbyService.assignTeam(lobbyCodeFor6, teamId, l.toString());
        });

        Assertions.assertDoesNotThrow(() -> lobbyService.startGame(lobbyCodeFor6, hostIdFor6));
    }

    @Test
    void startingUnknownLobbyShouldFail() {
        Assertions.assertThrows(NotFoundException.class, () -> lobbyService.startGame("INVALID", hostIdFor4));
    }

    @Test
    void startingLobbyWithWrongHostIdShouldFailFor4() {
        UUID unknowId = UUID.randomUUID();

        Assertions.assertNotEquals(unknowId, hostIdFor4);
        Assertions.assertThrows(NoAutoriseOperationException.class, () -> lobbyService.startGame(lobbyCodeFor4, unknowId));
    }

    @Test
    void startingLobbyWithWrongHostIdShouldFailFor6() {
        UUID unknowId = UUID.randomUUID();

        Assertions.assertNotEquals(unknowId, hostIdFor6);
        Assertions.assertThrows(NoAutoriseOperationException.class, () -> lobbyService.startGame(lobbyCodeFor6, unknowId));
    }

    @Test
    void ensureHostShouldSucceedForHost() {
        Lobby lobby = lobbyService.getLobby(lobbyCodeFor6);

        Assertions.assertDoesNotThrow(() -> lobbyService.ensureHost(lobby, hostIdFor6));
    }

    @Test
    void ensureHostShouldFailForUnknownHost() {
        Lobby lobby = lobbyService.getLobby(lobbyCodeFor6);
        UUID unknowId = UUID.randomUUID();

        Assertions.assertNotEquals(unknowId, hostIdFor6);
        Assertions.assertThrows(NoAutoriseOperationException.class, () -> lobbyService.ensureHost(lobby, unknowId));
    }

    @Test
    void ensureHostShouldFailForDifferentLobby() {
        LobbyCreationResponse newLobbyRes = lobbyService.createLobby(4);
        String newLobbyCode = newLobbyRes.lobbyCode();
        Lobby newLobby = lobbyService.getLobby(newLobbyCode);

        Assertions.assertThrows(NoAutoriseOperationException.class, () -> lobbyService.ensureHost(newLobby, hostIdFor4));
    }

    @Test
    void ensureClientShouldSucceedForClient() {
        Lobby lobby = lobbyService.getLobby(lobbyCodeFor6);
        LobbyJoinedResponse res = lobbyService.joinLobby(lobbyCodeFor6);
        UUID clientId = UUID.fromString(res.clientId());

        Assertions.assertDoesNotThrow(() -> lobbyService.ensureClient(lobby, clientId));
    }

    @Test
    void ensureClientShouldFailForUnknownClient() {
        Lobby lobby = lobbyService.getLobby(lobbyCodeFor6);
        LobbyJoinedResponse res = lobbyService.joinLobby(lobbyCodeFor6);
        UUID clientId = UUID.fromString(res.clientId());

        UUID unknowId = UUID.randomUUID();

        Assertions.assertNotEquals(unknowId, clientId);
        Assertions.assertThrows(NoAutoriseOperationException.class, () -> lobbyService.ensureClient(lobby, unknowId));
    }

    @Test
    void ensureClientShouldFailForDifferentLobby() {
        LobbyJoinedResponse res = lobbyService.joinLobby(lobbyCodeFor6);
        UUID clientId = UUID.fromString(res.clientId());

        LobbyCreationResponse newLobbyRes = lobbyService.createLobby(4);
        String newLobbyCode = newLobbyRes.lobbyCode();
        Lobby newLobby = lobbyService.getLobby(newLobbyCode);

        Assertions.assertThrows(NoAutoriseOperationException.class, () -> lobbyService.ensureHost(newLobby, clientId));
    }

    @Test
    void shouldClearOnlyLobby12HoursOld() {
        Map<String, Lobby> shouldBeThere = Map.of(
                "1", new Lobby(new Host(UUID.randomUUID()), 4, LocalDateTime.now()),
                "2", new Lobby(new Host(UUID.randomUUID()), 4, LocalDateTime.now().minusSeconds(1)),
                "3", new Lobby(new Host(UUID.randomUUID()), 4, LocalDateTime.now().minusMinutes(1)),
                "4", new Lobby(new Host(UUID.randomUUID()), 4, LocalDateTime.now().minusMinutes(30)),
                "5", new Lobby(new Host(UUID.randomUUID()), 4, LocalDateTime.now().minusHours(1)),
                "6", new Lobby(new Host(UUID.randomUUID()), 4, LocalDateTime.now().minusHours(6)),
                "7", new Lobby(new Host(UUID.randomUUID()), 4, LocalDateTime.now().minusHours(11)));

        Map<String, Lobby> shouldNotBeThere = Map.of(
                "A", new Lobby(new Host(UUID.randomUUID()), 4, LocalDateTime.now().minusHours(12)),
                "B", new Lobby(new Host(UUID.randomUUID()), 4, LocalDateTime.now().minusHours(24)),
                "C", new Lobby(new Host(UUID.randomUUID()), 4, LocalDateTime.now().minusHours(36)),
                "D", new Lobby(new Host(UUID.randomUUID()), 4, LocalDateTime.now().minusDays(1)),
                "E", new Lobby(new Host(UUID.randomUUID()), 4, LocalDateTime.now().minusDays(7)),
                "F", new Lobby(new Host(UUID.randomUUID()), 4, LocalDateTime.now().minusMonths(1)));

        shouldBeThere.forEach((k, v) -> lobbyService.addLobby(k, v));
        shouldNotBeThere.forEach((k, v) -> lobbyService.addLobby(k, v));

        lobbyService.clearLobbies();

        shouldBeThere.forEach((k, v) -> Assertions.assertTrue(lobbyService.lobbies.containsKey(k)));
        shouldNotBeThere.forEach((k, v) -> Assertions.assertFalse(lobbyService.lobbies.containsKey(k)));
    }

    @Test
    void getLobbyInfoShouldFailForUnknown() {
        Assertions.assertThrows(NotFoundException.class, () -> lobbyService.getLobbyInfo("INVALID"));
    }

    @Test
    void getLobbyInfoForFour() {
        LobbyInfoResponse res = lobbyService.getLobbyInfo(lobbyCodeFor4);

        Assertions.assertEquals(4, res.allTeams().size());
        Assertions.assertEquals(4, res.availableTeams().size());
    }

    @Test
    void getLobbyInfoForSix() {
        LobbyInfoResponse res = lobbyService.getLobbyInfo(lobbyCodeFor6);

        Assertions.assertEquals(6, res.allTeams().size());
        Assertions.assertEquals(6, res.availableTeams().size());
    }

    @Test
    void getLobbyInfoShouldDecreaseAtEachJoinedFor4() {
        for (int i = 4; i > 0; i--) {
            LobbyJoinedResponse joinedResponse = lobbyService.joinLobby(lobbyCodeFor4);

            UUID clientId = UUID.fromString(joinedResponse.clientId());

            LobbyInfoResponse res = lobbyService.getLobbyInfo(lobbyCodeFor4);

            Assertions.assertEquals(4, res.allTeams().size());
            Assertions.assertEquals(i, res.availableTeams().size());

            lobbyService.assignTeam(lobbyCodeFor4, clientId, res.availableTeams().get(i - 1));
        }
    }

    @Test
    void getLobbyInfoShouldDecreaseAtEachJoinedFor6() {
        for (int i = 6; i > 0; i--) {
            LobbyJoinedResponse joinedResponse = lobbyService.joinLobby(lobbyCodeFor6);

            UUID clientId = UUID.fromString(joinedResponse.clientId());

            LobbyInfoResponse res = lobbyService.getLobbyInfo(lobbyCodeFor6);

            Assertions.assertEquals(6, res.allTeams().size());
            Assertions.assertEquals(i, res.availableTeams().size());

            lobbyService.assignTeam(lobbyCodeFor6, clientId, res.availableTeams().get(i - 1));
        }
    }
}
