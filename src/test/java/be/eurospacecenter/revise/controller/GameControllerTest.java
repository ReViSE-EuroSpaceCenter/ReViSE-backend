package be.eurospacecenter.revise.controller;

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

    @BeforeEach
    void setUp() {
        var result = restTestClient.post().uri("/api/lobbies").exchange().expectStatus().isOk().expectBody().returnResult();

        String body = new String(result.getResponseBody());
        lobbyCode = JsonPath.read(body, "$.lobbyCode");

        lobbyService.joinLobby(lobbyCode, "EXPE");

        Lobby lobby = lobbyService.getLobby(lobbyCode);

        Game game = new Game(lobby.getHost(), lobby.getTeams());

        gameService.registerGame(lobbyCode, game);
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
