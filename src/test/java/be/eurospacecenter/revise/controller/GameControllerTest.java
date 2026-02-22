package be.eurospacecenter.revise.controller;

import be.eurospacecenter.revise.dto.response.LobbyJoinedResponse;
import be.eurospacecenter.revise.model.TeamLabel;
import be.eurospacecenter.revise.service.LobbyService;
import com.jayway.jsonpath.JsonPath;
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

        String body = new String(result.getResponseBody());
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
    void completeMissionShouldSucceed() {
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
    void completeMissionShouldFailWithNonExistingGame() {
        restTestClient.put()
                .uri("/api/games/XXXXXX/missions")
                .body(Map.of(
                        "clientId", teamClientId,
                        "missionNumber", "CLASSIC_1"
                ))
                .exchange()
                .expectStatus().isBadRequest();
    }
}
