package be.eurospacecenter.revise.controller;

import be.eurospacecenter.revise.dto.response.LobbyCreationResponse;
import be.eurospacecenter.revise.dto.response.LobbyJoinedResponse;
import org.junit.jupiter.api.Assertions;
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

    /* ====================
       LOBBY CREATION
       ==================== */

    @Test
    void lobbyShouldReturnLobbyCode() {
        restTestClient.post().uri(uriBuilder -> uriBuilder.path("/api/lobbies").queryParam("numberOfTeams", 4).build()).exchange().expectStatus().isCreated().expectBody().jsonPath("$.lobbyCode").exists();
    }

    @Test
    void lobbyShouldReturnHostId() {
        restTestClient.post().uri(uriBuilder -> uriBuilder.path("/api/lobbies").queryParam("numberOfTeams", 4).build()).exchange().expectStatus().isCreated().expectBody().jsonPath("$.hostId").exists();
    }

    @Test
    void lobbyShouldFailForInvalidNumberOfTeams() {
        restTestClient.post().uri(uriBuilder -> uriBuilder.path("/api/lobbies").queryParam("numberOfTeams", 3).build()).exchange().expectStatus().isBadRequest();
    }

    /* ====================
       JOIN LOBBY
       ==================== */

    @Test
    void joinLobbyShouldSucceed() {
        LobbyCreationResponse lobby = createLobby(4);

        restTestClient.post().uri("/api/lobbies/{lobbyCode}/join", lobby.lobbyCode()).exchange().expectStatus().isOk();
    }

    @Test
    void joinLobbyShouldFailForUnkownLobbyCode() {
        restTestClient.post().uri("/api/lobbies/{lobbyCode}/join", "INVALD").exchange().expectStatus().isNotFound();
    }

    @Test
    void joinLobbyShouldFailForInvalidLobbyCode() {
        restTestClient.post().uri("/api/lobbies/{lobbyCode}/join", "INVALID").exchange().expectStatus().isBadRequest();
    }

    @Test
    void joinLobbyShouldFailForDuplicateTeamLabel() {
        LobbyCreationResponse lobby = createLobby(4);

        LobbyJoinedResponse firstClient = joinLobby(lobby.lobbyCode());
        assignTeam(lobby.lobbyCode(), firstClient.clientId(), "INGE");

        LobbyJoinedResponse secondClient = joinLobby(lobby.lobbyCode());
        restTestClient.post().uri(uriBuilder -> uriBuilder.path("/api/lobbies/{lobbyCode}/team").queryParam("clientId", secondClient.clientId()).queryParam("teamLabel", "INGE").build(lobby.lobbyCode())).exchange().expectStatus().isBadRequest();
    }

    @Test
    void joinLobbyShouldSucceedForDifferentTeamLabels() {
        LobbyCreationResponse lobby = createLobby(4);

        LobbyJoinedResponse firstClient = joinLobby(lobby.lobbyCode());
        assignTeam(lobby.lobbyCode(), firstClient.clientId(), "INGE");

        LobbyJoinedResponse secondClient = joinLobby(lobby.lobbyCode());
        assignTeam(lobby.lobbyCode(), secondClient.clientId(), "GECO");
    }

    @Test
    void joinLobbyShouldFailForUnkownClient() {
        LobbyCreationResponse lobby = createLobby(4);

        LobbyJoinedResponse client = joinLobby(lobby.lobbyCode());
        UUID newClientId = UUID.randomUUID();

        Assertions.assertNotEquals(client.clientId(), newClientId.toString());

        restTestClient.post().uri(uriBuilder -> uriBuilder.path("/api/lobbies/{lobbyCode}/team").queryParam("clientId", newClientId).queryParam("teamLabel", "INGE").build(lobby.lobbyCode())).exchange().expectStatus().isForbidden();
    }

    /* ====================
       START LOBBY
       ==================== */

    @Test
    void startLobbyShouldFailForInvalidLobbyCode() {
        restTestClient.post().uri(uriBuilder -> uriBuilder.path("/api/lobbies/{lobbyCode}/start").queryParam("hostId", UUID.randomUUID()).build("INVALID")).exchange().expectStatus().isBadRequest();
    }

    @Test
    void startLobbyShouldFailForDifferentHostId() {
        LobbyCreationResponse lobby = createLobby(4);

        restTestClient.post().uri(uriBuilder -> uriBuilder.path("/api/lobbies/{lobbyCode}/start").queryParam("hostId", UUID.randomUUID()).build(lobby.lobbyCode())).exchange().expectStatus().isForbidden();
    }

    @Test
    void startGameShouldFailForUnassignedTeam() {
        LobbyCreationResponse lobby = createLobby(4);

        restTestClient.post().uri(uriBuilder -> uriBuilder.path("/api/lobbies/{lobbyCode}/start").queryParam("hostId", lobby.hostId()).build(lobby.lobbyCode())).exchange().expectStatus().isBadRequest();
    }

    /* ====================
       HELPERS
       ==================== */

    private LobbyCreationResponse createLobby(int numberOfTeams) {
        return restTestClient.post().uri(uriBuilder -> uriBuilder.path("/api/lobbies").queryParam("numberOfTeams", numberOfTeams).build()).exchange().expectStatus().isCreated().expectBody(LobbyCreationResponse.class).returnResult().getResponseBody();
    }

    private LobbyJoinedResponse joinLobby(String lobbyCode) {
        return restTestClient.post().uri("/api/lobbies/{lobbyCode}/join", lobbyCode).exchange().expectStatus().isOk().expectBody(LobbyJoinedResponse.class).returnResult().getResponseBody();
    }

    private void assignTeam(String lobbyCode, String clientId, String teamLabel) {
        restTestClient.post().uri(uriBuilder -> uriBuilder.path("/api/lobbies/{lobbyCode}/team").queryParam("clientId", clientId).queryParam("teamLabel", teamLabel).build(lobbyCode)).exchange().expectStatus().isNoContent();
    }
}
