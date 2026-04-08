package be.eurospacecenter.revise.controller;

import be.eurospacecenter.revise.exceptions.ErrorKeys;
import be.eurospacecenter.revise.model.lobby.LobbyJoined;
import be.eurospacecenter.revise.model.lobby.TeamLabel;
import be.eurospacecenter.revise.model.lobbycode.LobbyCode;
import be.eurospacecenter.revise.model.mission.MissionType;
import be.eurospacecenter.revise.service.LobbyService;
import be.eurospacecenter.revise.service.MissionService;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
class LauncherControllerTest {

    @Autowired
    private RestTestClient restTestClient;

    @Autowired
    private LobbyService lobbyService;

    @Autowired
    private MissionService missionService;

    private LobbyCode lobbyCode;
    private UUID hostId;

    // ---------------------------------------------------------------------
    // Setup
    // ---------------------------------------------------------------------

    @BeforeEach
    void setUp() {
        var result = restTestClient.post()
                .uri("/api/lobbies")
                .body(Map.of("numberOfTeams", 4))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .returnResult();

        Assertions.assertNotNull(result.getResponseBody());
        var body = new String(result.getResponseBody());
        lobbyCode = new LobbyCode(JsonPath.read(body, "$.lobbyCode"));
        hostId = UUID.fromString(JsonPath.read(body, "$.hostId"));

        for (TeamLabel label : TeamLabel.getAllowedLabels(true)) {
            LobbyJoined join = lobbyService.joinLobby(lobbyCode);
            UUID cid = UUID.fromString(join.clientId());
            lobbyService.assignTeam(lobbyCode, cid, label);
        }

        lobbyService.startGame(lobbyCode, hostId);

        // Complete missions and end game to enter launcher phase
        TeamLabel.getAllowedLabels(true).forEach(teamLabel -> {
            Set<MissionType> missions = MissionType.getClassicMissions();
            if (teamLabel != TeamLabel.MECA) {
                missions.remove(MissionType.CLASSIC_8);
            }
            missionService.changeTeamMissionsState(lobbyCode, hostId, teamLabel, missions);
        });
        missionService.endMission(lobbyCode, hostId);
    }

    // ---------------------------------------------------------------------
    // Tests
    // ---------------------------------------------------------------------

    @Test
    void getTeamsFullProgression_shouldSucceed() {
        restTestClient.get()
                .uri("/api/launcher/" + lobbyCode.lobbyCode())
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.teamsFullProgression").isMap();
    }

    @Test
    void getTeamsFullProgression_shouldFail_withInvalidLobbyCode() {
        restTestClient.get()
                .uri("/api/launcher/INVALID")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody().jsonPath("$.detail").isEqualTo(ErrorKeys.INVALID_LOBBY_CODE);
    }

    @Test
    void endLauncher_shouldSucceed_WithValidLobbyCodeHostId() {
        restTestClient.post()
                .uri("/api/launcher/" + lobbyCode.lobbyCode() + "/end")
                .body(Map.of("hostId", hostId))
                .exchange()
                .expectStatus()
                .isNoContent();
    }

    @Test
    void endLauncher_shouldFail_withInvalidLobbyCode() {
        restTestClient.post()
                .uri("/api/launcher/INVALID/end")
                .body(Map.of("hostId", hostId))
                .exchange()
                .expectStatus()
                .isBadRequest();
    }

    @Test
    void endLauncher_shouldFail_withUnknownHost() {
        restTestClient.post()
                .uri("/api/launcher/" + lobbyCode.lobbyCode() + "/end")
                .body(Map.of("hostId", UUID.randomUUID()))
                .exchange()
                .expectStatus()
                .isForbidden();
    }

    @Test
    void endGame_shouldSucceed_WithValidLobbyCodeHostId() {
        restTestClient.post()
                .uri("/api/launcher/" + lobbyCode.lobbyCode() + "/gameOver")
                .body(Map.of("hostId", hostId))
                .exchange()
                .expectStatus()
                .isNoContent();
    }

    @Test
    void endGame_shouldFail_withInvalidLobbyCode() {
        restTestClient.post()
                .uri("/api/launcher/INVALID/gameOver")
                .body(Map.of("hostId", hostId))
                .exchange()
                .expectStatus()
                .isBadRequest();
    }

    @Test
    void endGame_shouldFail_withUnknownHost() {
        restTestClient.post()
                .uri("/api/launcher/" + lobbyCode.lobbyCode() + "/gameOver")
                .body(Map.of("hostId", UUID.randomUUID()))
                .exchange()
                .expectStatus()
                .isForbidden();
    }

    @Test
    void illegalStateShouldBeReturnedWhenTryingToGetProgressionAfterLauncherEnded() {
        restTestClient.post()
                .uri("/api/launcher/" + lobbyCode.lobbyCode() + "/end")
                .body(Map.of("hostId", hostId))
                .exchange()
                .expectStatus()
                .isNoContent();

        restTestClient.get()
                .uri("/api/launcher/" + lobbyCode.lobbyCode())
                .exchange()
                .expectStatus()
                .is4xxClientError()
                .expectBody().jsonPath("$.error").isEqualTo(ErrorKeys.INVALID_GAME_STATE);
    }
}
