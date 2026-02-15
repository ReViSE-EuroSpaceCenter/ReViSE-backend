package be.eurospacecenter.revise.service;

import be.eurospacecenter.revise.dto.response.LobbyCreationResponse;
import be.eurospacecenter.revise.dto.response.LobbyJoinedResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.util.Map;
import java.util.UUID;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
class LobbyServiceTest {

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
    void joinLobbyShouldSucceed() {
        LobbyCreationResponse lobby = createLobby(4);

        restTestClient.post()
                .uri("/api/lobbies/{lobbyCode}/join", lobby.lobbyCode())
                .exchange()
                .expectStatus().isOk();
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
        assignTeam(lobby.lobbyCode(), firstClient.clientId(), "INGE");

        LobbyJoinedResponse secondClient = joinLobby(lobby.lobbyCode());

        restTestClient.post()
                .uri("/api/lobbies/{lobbyCode}/team", lobby.lobbyCode())
                .body(Map.of(
                        "clientId", secondClient.clientId(),
                        "teamLabel", "INGE"
                ))
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void joinLobbyShouldSucceedForDifferentTeamLabels() {
        LobbyCreationResponse lobby = createLobby(4);

        LobbyJoinedResponse firstClient = joinLobby(lobby.lobbyCode());
        assignTeam(lobby.lobbyCode(), firstClient.clientId(), "INGE");

        LobbyJoinedResponse secondClient = joinLobby(lobby.lobbyCode());
        assignTeam(lobby.lobbyCode(), secondClient.clientId(), "GECO");
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
