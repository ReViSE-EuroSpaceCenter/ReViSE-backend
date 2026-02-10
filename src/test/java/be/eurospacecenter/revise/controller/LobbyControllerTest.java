package be.eurospacecenter.revise.controller;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.test.web.servlet.client.RestTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
class LobbyControllerTest {

    @Autowired
    private RestTestClient restTestClient;

    @Test
    void lobbyShouldReturnLobbyCode() {
        restTestClient.post().uri("/api/lobbies").exchange().expectStatus().isOk().expectBody().jsonPath("$.lobbyCode").exists();
    }

    @Test
    void joinLobbyShouldSucceed() {
        var result = restTestClient.post().uri("/api/lobbies").exchange().expectStatus().isOk().expectBody().returnResult();

        String body = new String(result.getResponseBody());
        String lobbyCode = JsonPath.read(body, "$.lobbyCode");

        restTestClient.post().uri(uriBuilder -> uriBuilder.path("/api/lobbies/{lobbyCode}/join").queryParam("teamLabel", "ING").build(lobbyCode)).exchange().expectStatus().isOk();
    }

    @Test
    void joinLobbyShouldFailForInvalidLobbyCode() {
        restTestClient.post().uri(uriBuilder -> uriBuilder.path("/api/lobbies/{lobbyCode}/join").queryParam("teamLabel", "ING").build("INVALID_CODE")).exchange().expectStatus().isBadRequest();
    }

    @Test
    void joinLobbyShouldFailForDuplicateTeamLabel() {
        var result = restTestClient.post().uri("/api/lobbies").exchange().expectStatus().isOk().expectBody().returnResult();

        String body = new String(result.getResponseBody());
        String lobbyCode = JsonPath.read(body, "$.lobbyCode");

        restTestClient.post().uri(uriBuilder -> uriBuilder.path("/api/lobbies/{lobbyCode}/join").queryParam("teamLabel", "ING").build(lobbyCode)).exchange().expectStatus().isOk();
        restTestClient.post().uri(uriBuilder -> uriBuilder.path("/api/lobbies/{lobbyCode}/join").queryParam("teamLabel", "ING").build(lobbyCode)).exchange().expectStatus().isNotFound();
    }

    @Test
    void joinLobbyShouldSucceedForDifferentTeamLabels() {
        var result = restTestClient.post().uri("/api/lobbies").exchange().expectStatus().isOk().expectBody().returnResult();

        String body = new String(result.getResponseBody());
        String lobbyCode = JsonPath.read(body, "$.lobbyCode");

        restTestClient.post().uri(uriBuilder -> uriBuilder.path("/api/lobbies/{lobbyCode}/join").queryParam("teamLabel", "ING").build(lobbyCode)).exchange().expectStatus().isOk();
        restTestClient.post().uri(uriBuilder -> uriBuilder.path("/api/lobbies/{lobbyCode}/join").queryParam("teamLabel", "MED").build(lobbyCode)).exchange().expectStatus().isOk();
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

        restTestClient.post().uri(uriBuilder -> uriBuilder.path("/api/lobbies/{lobbyCode}/join").queryParam("teamLabel", "LOL").build(lobbyCode)).exchange().expectStatus().isNotFound();
    }
}
