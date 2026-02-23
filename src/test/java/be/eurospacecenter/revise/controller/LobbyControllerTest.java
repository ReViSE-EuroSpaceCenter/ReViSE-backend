package be.eurospacecenter.revise.controller;

import be.eurospacecenter.revise.dto.response.LobbyCreationResponse;
import be.eurospacecenter.revise.dto.response.LobbyJoinedResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.util.Map;
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
        restTestClient.post()
                .uri("/api/lobbies")
                .body(Map.of("numberOfTeams", 4))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.lobbyCode").exists();
    }

    @Test
    void lobbyShouldReturnHostId() {
        restTestClient.post()
                .uri("/api/lobbies")
                .body(Map.of("numberOfTeams", 4))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.hostId").exists();
    }

    @Test
    void lobbyShouldFailForInvalidNumberOfTeams() {
        restTestClient.post()
                .uri("/api/lobbies")
                .body(Map.of("numberOfTeams", 3))
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void getInfoForLobbyShouldSucceed() {
        LobbyCreationResponse lobby = createLobby(4);

        restTestClient.get()
                .uri("/api/lobbies/{lobbyCode}", lobby.lobbyCode())
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void getInfoForUnknownLobbyShouldFailed() {
        restTestClient.get()
                .uri("/api/lobbies/{lobbyCode}", "INVALD")
                .exchange()
                .expectStatus().isNotFound();
    }

    /* ====================
       JOIN LOBBY
       ==================== */

    @Test
    void joinLobbyShouldSucceed() {
        LobbyCreationResponse lobby = createLobby(4);

        restTestClient.post()
                .uri("/api/lobbies/{lobbyCode}/join", lobby.lobbyCode())
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void joinLobbyShouldFailForUnkownLobbyCode() {
        restTestClient.post()
                .uri("/api/lobbies/{lobbyCode}/join", "INVALD")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void joinLobbyShouldFailForInvalidLobbyCode() {
        restTestClient.post()
                .uri("/api/lobbies/{lobbyCode}/join", "INVALID")
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void joinLobbyShouldFailForDuplicateTeamLabel() {
        LobbyCreationResponse lobby = createLobby(4);

        LobbyJoinedResponse firstClient = joinLobby(lobby.lobbyCode());
        assignTeam(lobby.lobbyCode(), firstClient.clientId(), "AERO");

        LobbyJoinedResponse secondClient = joinLobby(lobby.lobbyCode());

        restTestClient.post()
                .uri("/api/lobbies/{lobbyCode}/team", lobby.lobbyCode())
                .body(Map.of(
                        "clientId", secondClient.clientId(),
                        "teamLabel", "AERO"
                ))
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void joinLobbyShouldSucceedForDifferentTeamLabels() {
        LobbyCreationResponse lobby = createLobby(4);

        LobbyJoinedResponse firstClient = joinLobby(lobby.lobbyCode());
        assignTeam(lobby.lobbyCode(), firstClient.clientId(), "AERO");

        LobbyJoinedResponse secondClient = joinLobby(lobby.lobbyCode());
        assignTeam(lobby.lobbyCode(), secondClient.clientId(), "GECO");
    }

    @Test
    void joinLobbyShouldFailForUnkownClient() {
        LobbyCreationResponse lobby = createLobby(4);

        LobbyJoinedResponse client = joinLobby(lobby.lobbyCode());
        UUID newClientId = UUID.randomUUID();

        Assertions.assertNotEquals(client.clientId(), newClientId.toString());

        restTestClient.post()
                .uri("/api/lobbies/{lobbyCode}/team", lobby.lobbyCode())
                .body(Map.of(
                        "clientId", newClientId,
                        "teamLabel", "AERO"
                ))
                .exchange()
                .expectStatus().isForbidden();
    }

    /* ====================
       START LOBBY
       ==================== */

    @Test
    void startLobbyShouldFailForInvalidLobbyCode() {
        restTestClient.post()
                .uri("/api/lobbies/{lobbyCode}/start", "INVALID")
                .body(Map.of("hostId", UUID.randomUUID()))
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void startLobbyShouldFailForDifferentHostId() {
        LobbyCreationResponse lobby = createLobby(4);

        restTestClient.post()
                .uri("/api/lobbies/{lobbyCode}/start", lobby.lobbyCode())
                .body(Map.of("hostId", UUID.randomUUID()))
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void startGameShouldFailForUnassignedTeam() {
        LobbyCreationResponse lobby = createLobby(4);

        restTestClient.post()
                .uri("/api/lobbies/{lobbyCode}/start", lobby.lobbyCode())
                .body(Map.of("hostId", lobby.hostId()))
                .exchange()
                .expectStatus().isBadRequest();
    }

    /* ====================
       HELPERS
       ==================== */

    private LobbyCreationResponse createLobby(int numberOfTeams) {
        return restTestClient.post()
                .uri("/api/lobbies")
                .body(Map.of("numberOfTeams", numberOfTeams))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(LobbyCreationResponse.class)
                .returnResult()
                .getResponseBody();
    }

    private LobbyJoinedResponse joinLobby(String lobbyCode) {
        return restTestClient.post()
                .uri("/api/lobbies/{lobbyCode}/join", lobbyCode)
                .exchange()
                .expectStatus().isOk()
                .expectBody(LobbyJoinedResponse.class)
                .returnResult()
                .getResponseBody();
    }

    private void assignTeam(String lobbyCode, String clientId, String teamLabel) {
        restTestClient.post()
                .uri("/api/lobbies/{lobbyCode}/team", lobbyCode)
                .body(Map.of(
                        "clientId", clientId,
                        "teamLabel", teamLabel
                ))
                .exchange()
                .expectStatus().isNoContent();
    }
}
