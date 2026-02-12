package be.eurospacecenter.revise.controller;

import be.eurospacecenter.revise.model.Lobby;
import be.eurospacecenter.revise.service.LobbyService;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.util.List;
import java.util.UUID;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
class LobbyControllerTest {

    @Autowired
    private RestTestClient restTestClient;

    @Autowired
    private LobbyService lobbyService;

    @Test
    void lobbyShouldReturnLobbyCode() {
        restTestClient.post().uri("/api/lobbies").exchange().expectStatus().isOk().expectBody().jsonPath("$.lobbyCode").exists();
    }

    @Test
    void joinLobbyShouldSucceed() {
        var result = restTestClient.post().uri("/api/lobbies").exchange().expectStatus().isOk().expectBody().returnResult();

        String body = new String(result.getResponseBody());
        String lobbyCode = JsonPath.read(body, "$.lobbyCode");

        restTestClient.post().uri(uriBuilder -> uriBuilder.path("/api/lobbies/{lobbyCode}/join").queryParam("teamLabel", "INGE").build(lobbyCode)).exchange().expectStatus().isOk();
    }

    @Test
    void joinLobbyShouldFailForInvalidLobbyCode() {
        restTestClient.post().uri(uriBuilder -> uriBuilder.path("/api/lobbies/{lobbyCode}/join").queryParam("teamLabel", "INGE").build("INVALID_CODE")).exchange().expectStatus().isBadRequest();
    }

    @Test
    void joinLobbyShouldFailForDuplicateTeamLabel() {
        var result = restTestClient.post().uri("/api/lobbies").exchange().expectStatus().isOk().expectBody().returnResult();

        String body = new String(result.getResponseBody());
        String lobbyCode = JsonPath.read(body, "$.lobbyCode");

        restTestClient.post().uri(uriBuilder -> uriBuilder.path("/api/lobbies/{lobbyCode}/join").queryParam("teamLabel", "INGE").build(lobbyCode)).exchange().expectStatus().isOk();
        restTestClient.post().uri(uriBuilder -> uriBuilder.path("/api/lobbies/{lobbyCode}/join").queryParam("teamLabel", "INGE").build(lobbyCode)).exchange().expectStatus().isBadRequest();
    }

    @Test
    void joinLobbyShouldSucceedForDifferentTeamLabels() {
        var result = restTestClient.post().uri("/api/lobbies").exchange().expectStatus().isOk().expectBody().returnResult();

        String body = new String(result.getResponseBody());
        String lobbyCode = JsonPath.read(body, "$.lobbyCode");

        restTestClient.post().uri(uriBuilder -> uriBuilder.path("/api/lobbies/{lobbyCode}/join").queryParam("teamLabel", "INGE").build(lobbyCode)).exchange().expectStatus().isOk();
        restTestClient.post().uri(uriBuilder -> uriBuilder.path("/api/lobbies/{lobbyCode}/join").queryParam("teamLabel", "MEDI").build(lobbyCode)).exchange().expectStatus().isOk();
    }

    @Test
    void joinLobbyShouldFailForEmptyTeamLabel() {
        var result = restTestClient.post().uri("/api/lobbies").exchange().expectStatus().isOk().expectBody().returnResult();

        String body = new String(result.getResponseBody());
        String lobbyCode = JsonPath.read(body, "$.lobbyCode");

        restTestClient.post().uri(uriBuilder -> uriBuilder.path("/api/lobbies/{lobbyCode}/join").queryParam("teamLabel", "").build(lobbyCode)).exchange().expectStatus().isBadRequest();
    }

    @Test
    void joinLobbyShouldFailForNullTeamLabel() {
        var result = restTestClient.post().uri("/api/lobbies").exchange().expectStatus().isOk().expectBody().returnResult();

        String body = new String(result.getResponseBody());
        String lobbyCode = JsonPath.read(body, "$.lobbyCode");

        restTestClient.post().uri(uriBuilder -> uriBuilder.path("/api/lobbies/{lobbyCode}/join").queryParam("teamLabel", (String) null).build(lobbyCode)).exchange().expectStatus().isBadRequest();
    }

    @Test
    void joinLobbyShouldFailForNoExistingTeamLabel() {
        var result = restTestClient.post().uri("/api/lobbies").exchange().expectStatus().isOk().expectBody().returnResult();

        String body = new String(result.getResponseBody());
        String lobbyCode = JsonPath.read(body, "$.lobbyCode");

        restTestClient.post().uri(uriBuilder -> uriBuilder.path("/api/lobbies/{lobbyCode}/join").queryParam("teamLabel", "LOL").build(lobbyCode)).exchange().expectStatus().isBadRequest();
    }

    @ParameterizedTest
    @CsvSource({
            "1, false",
            "2, false",
            "3, false",
            "4, true",
            "5, false",
            "6, true"
    })
    void startLobbyShouldBehaveCorrectlyDependingOnTeamCount(int teamCount, boolean shouldSucceed) {
        var result = restTestClient.post().uri("/api/lobbies").exchange().expectStatus().isOk().expectBody().returnResult();

        String body = new String(result.getResponseBody());
        String lobbyCode = JsonPath.read(body, "$.lobbyCode");
        Lobby lobby = lobbyService.getLobby(lobbyCode);

        List<String> teamLabels = List.of("INGE", "MEDI", "COOP", "MECA", "EXPE", "GECO");

        for (int i = 0; i < teamCount; i++) {
            restTestClient.post().uri("/api/lobbies/" + lobbyCode + "/join?teamLabel=" + teamLabels.get(i)).exchange().expectStatus().isOk();
        }

        var startRequest = restTestClient.post().uri(uriBuilder -> uriBuilder.path("/api/lobbies/{lobbyCode}/start").queryParam("hostId", lobby.getHost().id()).build(lobbyCode)).exchange();

        if (shouldSucceed) {
            startRequest.expectStatus().isOk();
        } else {
            startRequest.expectStatus().isBadRequest();
        }
    }

    @Test
    void startLobbyShouldFailForBad4Teams() {
        var result = restTestClient.post().uri("/api/lobbies").exchange().expectStatus().isOk().expectBody().returnResult();

        String body = new String(result.getResponseBody());
        String lobbyCode = JsonPath.read(body, "$.lobbyCode");
        Lobby lobby = lobbyService.getLobby(lobbyCode);

        List<String> teamLabels = List.of("INGE", "MECA", "EXPE", "GECO");

        for (String teamLabel : teamLabels) {
            restTestClient.post().uri("/api/lobbies/" + lobbyCode + "/join?teamLabel=" + teamLabel).exchange().expectStatus().isOk();
        }
        restTestClient.post().uri(uriBuilder -> uriBuilder.path("/api/lobbies/{lobbyCode}/start").queryParam("hostId", lobby.getHost().id()).build(lobbyCode)).exchange().expectStatus().isBadRequest();
    }

    @Test
    void startLobbyShouldFailForInvalidLobbyCode() {
        restTestClient.post().uri(uriBuilder -> uriBuilder.path("/api/lobbies/{lobbyCode}/start").queryParam("hostId", UUID.randomUUID()).build("INVALID_CODE")).exchange().expectStatus().isBadRequest();
    }

    @Test
    void startLobbyShouldFailForDifferentHostId() {
        var result = restTestClient.post().uri("/api/lobbies").exchange().expectStatus().isOk().expectBody().returnResult();

        String body = new String(result.getResponseBody());
        String lobbyCode = JsonPath.read(body, "$.lobbyCode");

        restTestClient.post().uri(uriBuilder -> uriBuilder.path("/api/lobbies/{lobbyCode}/start").queryParam("hostId", UUID.randomUUID()).build(lobbyCode)).exchange().expectStatus().isBadRequest();
    }

    @Test
    void startLobbyShouldFailForNullHostId() {
        var result = restTestClient.post().uri("/api/lobbies").exchange().expectStatus().isOk().expectBody().returnResult();

        String body = new String(result.getResponseBody());
        String lobbyCode = JsonPath.read(body, "$.lobbyCode");

        restTestClient.post().uri(uriBuilder -> uriBuilder.path("/api/lobbies/{lobbyCode}/start").queryParam("hostId", (UUID) null).build(lobbyCode)).exchange().expectStatus().isBadRequest();
    }
}
