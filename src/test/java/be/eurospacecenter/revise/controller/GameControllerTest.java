package be.eurospacecenter.revise.controller;

import be.eurospacecenter.revise.dto.response.LobbyJoinedResponse;
import be.eurospacecenter.revise.exceptions.ErrorKeys;
import be.eurospacecenter.revise.model.TeamLabel;
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
class GameControllerTest {

    @Autowired
    private RestTestClient restTestClient;

    @Autowired
    private LobbyService lobbyService;

    private String lobbyCode;
    private UUID teamClientId;

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
        UUID hostId = UUID.fromString(JsonPath.read(body, "$.hostId"));

        List<TeamLabel> labels = TeamLabel.getAllowedLabels(true).stream().toList();

        for (int i = 0 ; i < 4 ; i++) {
            LobbyJoinedResponse response = lobbyService.joinLobby(lobbyCode);
            teamClientId = UUID.fromString(response.clientId());
            lobbyService.assignTeam(lobbyCode, teamClientId, labels.get(i).toString());
        }

        lobbyService.startGame(lobbyCode, hostId);
    }

    @Test
    void changeMissionStateShouldSucceed() {
        restTestClient.put()
                .uri("/api/games/" + lobbyCode + "/missions")
                .body(Map.of(
                    "clientId", teamClientId,
                    "missionNumber", "CLASSIC_1"
                ))
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void changeMissionStateShouldFailWithInvalidLobbyCode() {
        restTestClient.put()
                .uri("/api/games/INVALID/missions")
                .body(Map.of(
                        "clientId", teamClientId,
                        "missionNumber", "CLASSIC_1"
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
                .uri("/api/games/" + lobbyCode + "/missions")
                .body(Map.of(
                        "clientId", "abc",
                        "missionNumber", "CLASSIC_1"
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
                .uri("/api/games/" + lobbyCode + "/missions")
                .body(Map.of(
                        "clientId", teamClientId,
                        "missionNumber", "INVALID_MISSION"
                ))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.detail")
                .isEqualTo(ErrorKeys.INVALID_MISSION_TYPE);
    }

    @Test
    void getTeamMissionsShouldSucceed() {
        restTestClient.get()
                .uri("/api/games/" + lobbyCode + "/" + teamClientId + "/missions")
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
                .uri("/api/games/" + lobbyCode)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.teamsProgression").exists();
    }
}
