package be.eurospacecenter.revise.controller;

import be.eurospacecenter.revise.exceptions.ErrorKeys;
import be.eurospacecenter.revise.model.lobby.LobbyJoined;
import be.eurospacecenter.revise.model.lobby.TeamLabel;
import be.eurospacecenter.revise.model.lobbycode.LobbyCode;
import be.eurospacecenter.revise.service.LobbyService;
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
class MissionControllerTest {

    @Autowired private RestTestClient restTestClient;
    @Autowired private LobbyService lobbyService;

    private LobbyCode lobbyCode;
    private UUID teamClientId;
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
            teamClientId = cid;
        }

        lobbyService.startGame(lobbyCode, hostId);
    }

    // ---------------------------------------------------------------------
    // Tests
    // ---------------------------------------------------------------------

    @Test
    void changeMissionStateShouldSucceed() {
        putMission(Map.of("id", teamClientId, "updateMissions", Set.of("CLASSIC_1")))
                .expectStatus().isNoContent();
    }

    @Test
    void changeMissionStateShouldFailWithInvalidLobbyCode() {
        var response = restTestClient.put()
                .uri("/api/missions/INVALID")
                .body(Map.of("id", teamClientId, "updateMissions", Set.of("CLASSIC_1")))
                .exchange();

        expectBadRequest(ErrorKeys.INVALID_LOBBY_CODE, response);
    }

    @Test
    void changeMissionStateShouldFailWithInvalidUuid() {
        expectBadRequest(
                ErrorKeys.INVALID_UUID,
                putMission(Map.of("id", "abc", "updateMissions", Set.of("CLASSIC_1")))
        );
    }

    @Test
    void changeMissionStateShouldFailWithInvalidMission() {
        expectBadRequest(
                ErrorKeys.INVALID_MISSION_TYPE,
                putMission(Map.of("clientId", teamClientId, "updateMissions", Set.of("INVALID_MISSION")))
        );
    }

    @Test
    void changeMissionStateShouldSucceedForHost() {
        putMission(Map.of(
                "id", hostId,
                "teamLabel", "EXPE",
                "updateMissions", Set.of("CLASSIC_1")
        )).expectStatus().isNoContent();
    }

    @Test
    void changeMissionStateShouldNotSucceedForUnknownHost() {
        putMission(Map.of(
                "id", UUID.randomUUID(),
                "teamLabel", "EXPE",
                "updateMissions", Set.of("CLASSIC_1")
        )).expectStatus().isForbidden();
    }

    @Test
    void getTeamFullProgressionShouldSucceed() {
        restTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/missions/{lobbyCode}/team")
                        .queryParam("clientId", teamClientId)
                        .build(lobbyCode.lobbyCode()))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.completedMissions").exists()
                .jsonPath("$.teamProgressionDTO").exists();
    }

    @Test
    void getTeamsProgressionShouldSucceed() {
        restTestClient.get()
                .uri("/api/missions/" + lobbyCode.lobbyCode())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.teamsFullProgression").exists();
    }

    @Test
    void endMissionShouldNotSucceedWithInvalidLobbyCode() {
        var response = restTestClient.post()
                .uri("/api/missions/INVALID/end")
                .body(Map.of("hostId", teamClientId))
                .exchange();

        expectBadRequest(ErrorKeys.INVALID_LOBBY_CODE, response);
    }

    @Test
    void endMissionShouldNotSucceedWithUnknownHost() {
        var response = restTestClient.post()
                .uri("/api/missions/" + lobbyCode.lobbyCode() + "/end")
                .body(Map.of("hostId", UUID.randomUUID()))
                .exchange();

        expectReservedToHost(response);
    }

    @Test
    void endMissionShouldNotSucceedWithUncompletedMissions() {
        var response = restTestClient.post()
                .uri("/api/missions/" + lobbyCode.lobbyCode() + "/end")
                .body(Map.of("hostId", hostId))
                .exchange();

        expectBadRequest(ErrorKeys.LAUNCHER_START_INCOMPLETE_MISSIONS, response);
    }

    // ---------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------

    private void expectBadRequest(String expectedKey, RestTestClient.ResponseSpec response) {
        response.expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.detail")
                .isEqualTo(expectedKey);
    }

    private void expectReservedToHost(RestTestClient.ResponseSpec response) {
        response.expectStatus().isForbidden()
                .expectBody()
                .jsonPath("$.detail")
                .isEqualTo(ErrorKeys.ACTION_RESERVED_TO_HOST);
    }

    private RestTestClient.ResponseSpec putMission(Object body) {
        return restTestClient.put()
                .uri("/api/missions/" + lobbyCode.lobbyCode())
                .body(body)
                .exchange();
    }
}