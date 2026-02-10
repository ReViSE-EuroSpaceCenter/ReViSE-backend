package be.eurospacecenter.revise.controller;

import be.eurospacecenter.revise.model.Lobby;
import be.eurospacecenter.revise.service.LobbyService;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.test.web.servlet.client.RestTestClient;

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

    @Test
    void startLobbyShouldSucceed() {
        var result = restTestClient.post().uri("/api/lobbies").exchange().expectStatus().isOk().expectBody().returnResult();

        String body = new String(result.getResponseBody());
        String lobbyCode = JsonPath.read(body, "$.lobbyCode");
        Lobby lobby = lobbyService.getLobby(lobbyCode);

        restTestClient.post().uri(uriBuilder -> uriBuilder.path("/api/lobbies/{lobbyCode}/start").queryParam("hostId", lobby.getHostId()).build(lobbyCode)).exchange().expectStatus().isOk();
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
    void startLobbyShouldFailForLobbyAlreadyStarted() {
        var result = restTestClient.post().uri("/api/lobbies").exchange().expectStatus().isOk().expectBody().returnResult();

        String body = new String(result.getResponseBody());
        String lobbyCode = JsonPath.read(body, "$.lobbyCode");
        Lobby lobby = lobbyService.getLobby(lobbyCode);

        restTestClient.post().uri(uriBuilder -> uriBuilder.path("/api/lobbies/{lobbyCode}/start").queryParam("hostId", lobby.getHostId()).build(lobbyCode)).exchange().expectStatus().isOk();
        restTestClient.post().uri(uriBuilder -> uriBuilder.path("/api/lobbies/{lobbyCode}/start").queryParam("hostId", lobby.getHostId()).build(lobbyCode)).exchange().expectStatus().isBadRequest();
    }
}
