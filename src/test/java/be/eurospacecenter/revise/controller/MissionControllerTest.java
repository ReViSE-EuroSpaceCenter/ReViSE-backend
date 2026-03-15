package be.eurospacecenter.revise.controller;

import be.eurospacecenter.revise.dto.response.LobbyJoinedResponse;
import be.eurospacecenter.revise.exceptions.ErrorKeys;
import be.eurospacecenter.revise.model.lobby.TeamLabel;
import be.eurospacecenter.revise.service.LobbyService;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.util.List;
import java.util.Map;
import java.util.UUID;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
class MissionControllerTest {

    @Autowired
    private RestTestClient restTestClient;

    @Autowired
    private LobbyService lobbyService;

    private String lobbyCode;
    private UUID teamClientId;
    private UUID hostId;

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

        String body = new String(result.getResponseBody());

        Assertions.assertNotNull(body);

        lobbyCode = JsonPath.read(body, "$.lobbyCode");
        hostId = UUID.fromString(JsonPath.read(body, "$.hostId"));

        List<TeamLabel> labels = TeamLabel.getAllowedLabels(true).stream().toList();

        for (int i = 0; i < 4; i++) {
            LobbyJoinedResponse response = lobbyService.joinLobby(lobbyCode);
            teamClientId = UUID.fromString(response.clientId());
            lobbyService.assignTeam(lobbyCode, teamClientId, labels.get(i));
        }

        lobbyService.startGame(lobbyCode, hostId);
    }

    @Test
    void changeMissionStateShouldSucceed() {
        restTestClient.put()
                .uri("/api/missions/" + lobbyCode)
                .body(Map.of(
                        "id", teamClientId,
                        "updateMissions", List.of("CLASSIC_1")
                ))
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void changeMissionStateShouldFailWithInvalidLobbyCode() {
        restTestClient.put()
                .uri("/api/missions/INVALID")
                .body(Map.of(
                        "id", teamClientId,
                        "updateMissions", List.of("CLASSIC_1")
                ))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.detail")
                .isEqualTo(ErrorKeys.INVALID_LOBBY_CODE);
    }

    @Test
    void changeMissionStateShouldFailWithInvalidUuid() {
        restTestClient.put()
                .uri("/api/missions/" + lobbyCode)
                .body(Map.of(
                        "id", "abc",
                        "updateMissions", List.of("CLASSIC_1")
                ))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.detail")
                .isEqualTo(ErrorKeys.INVALID_UUID);
    }

    @Test
    void changeMissionStateShouldFailWithInvalidMission() {
        restTestClient.put()
                .uri("/api/missions/" + lobbyCode)
                .body(Map.of(
                        "clientId", teamClientId,
                        "updateMissions", List.of("INVALID_MISSION")
                ))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.detail")
                .isEqualTo(ErrorKeys.INVALID_MISSION_TYPE);
    }

    @Test
    void changeMissionStateShouldSucceedForHost() {
        restTestClient.put()
                .uri("/api/missions/" + lobbyCode)
                .body(Map.of(
                        "id", hostId,
                        "teamLabel", "EXPE",
                        "updateMissions", List.of("CLASSIC_1")
                ))
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void changeMissionStateShouldNotSucceedForUnknownHost() {
        restTestClient.put()
                .uri("/api/missions/" + lobbyCode)
                .body(Map.of(
                        "id", UUID.randomUUID(),
                        "teamLabel", "EXPE",
                        "updateMissions", List.of("CLASSIC_1")
                ))
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void getTeamMissionsShouldSucceed() {
        restTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/missions/{lobbyCode}")
                        .queryParam("clientId", teamClientId)
                        .build(lobbyCode))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.teamFullProgression").exists()
                .jsonPath("$.teamFullProgression.teamProgression").exists()
                .jsonPath("$.teamFullProgression.completedMissions").exists();
    }

    @Test
    void getTeamsProgressionShouldSucceed() {
        restTestClient.get()
                .uri("/api/missions/" + lobbyCode)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.teamsProgression").exists();
    }

    @Test
    void endMissionShouldNotSucceedWithInvalidLobbyCode() {
        restTestClient.post()
                .uri("/api/missions/INVALID/end")
                .body(Map.of(
                        "hostId", teamClientId
                ))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.detail")
                .isEqualTo(ErrorKeys.INVALID_LOBBY_CODE);
    }

    @Test
    void endMissionShouldNotSucceedWithUnkownHost() {
        restTestClient.post()
                .uri("/api/missions/" + lobbyCode + "/end")
                .body(Map.of(
                        "hostId", UUID.randomUUID()
                ))
                .exchange()
                .expectStatus().isForbidden()
                .expectBody()
                .jsonPath("$.detail")
                .isEqualTo(ErrorKeys.ACTION_RESERVED_TO_HOST);
    }

    @Test
    void endMissionShouldNotSucceedWithUncompletedMissions() {
        restTestClient.post()
                .uri("/api/missions/" + lobbyCode + "/end")
                .body(Map.of(
                        "hostId",hostId
                        ))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.detail")
                .isEqualTo(ErrorKeys.LAUNCHER_START_INCOMPLETE_MISSIONS);
    }
}
