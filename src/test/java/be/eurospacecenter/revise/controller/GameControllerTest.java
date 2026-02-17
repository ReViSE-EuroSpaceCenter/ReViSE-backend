package be.eurospacecenter.revise.controller;

import be.eurospacecenter.revise.dto.response.LobbyJoinedResponse;
import be.eurospacecenter.revise.model.Game;
import be.eurospacecenter.revise.model.Lobby;
import be.eurospacecenter.revise.service.GameService;
import be.eurospacecenter.revise.service.LobbyService;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.util.Map;
import java.util.UUID;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
class GameControllerTest {

    @Autowired
    private RestTestClient restTestClient;

    @Autowired
    private LobbyService lobbyService;

    @Autowired
    private GameService gameService;

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

        LobbyJoinedResponse response = lobbyService.joinLobby(lobbyCode);
        teamClientId = UUID.fromString(response.clientId());
        lobbyService.assignTeam(lobbyCode, teamClientId, "INGE");

        Lobby lobby = lobbyService.getLobby(lobbyCode);

        Game game = new Game(lobby.getTeams());
        gameService.registerGame(lobbyCode, game);
    }

    @Test
    void completeMissionShouldSucceed() {
        restTestClient.post()
                .uri("/api/games/" + lobbyCode + "/complete")
                .body(Map.of(
                    "clientId", teamClientId,
                    "missionNumber", "CLASSIC_1",
                    "resources",  Map.of(
                            "ENERGY", 1,
                            "HUMAN", 2
                    )
                ))
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void completeMissionShouldFailWithNonExistingGame() {
        restTestClient.post()
                .uri("/api/games/XXXXXX/complete")
                .body(Map.of(
                        "clientId", teamClientId,
                        "missionNumber", "CLASSIC_1",
                        "resources",  Map.of(
                                "ENERGY", 1,
                                "HUMAN", 2
                        )
                ))
                .exchange()
                .expectStatus().isBadRequest();
    }


    @Test
    void scoreShouldReturnScoreOfTheGame() {
        restTestClient.post()
                .uri("/api/games/" + lobbyCode + "/score")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.score").isEqualTo(25);
    }

    @Test
    void scoreShouldFailWithNonExistingGame() {
        restTestClient.post().uri("/api/games/" + "AAAAAA" + "/score" ).exchange().expectStatus().isBadRequest();
    }
}
