package be.eurospacecenter.revise.controller;

import be.eurospacecenter.revise.dto.lobby.LobbyCreationDTO;
import be.eurospacecenter.revise.dto.lobby.LobbyJoinedDTO;
import be.eurospacecenter.revise.exceptions.ErrorKeys;
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
    void lobbyShouldReturnHostIdFor4() {
        restTestClient.post()
                .uri("/api/lobbies")
                .body(Map.of("numberOfTeams", 4))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.hostId").exists();
    }

    @Test
    void lobbyShouldReturnHostIdFor6() {
        restTestClient.post()
                .uri("/api/lobbies")
                .body(Map.of("numberOfTeams", 6))
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
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.detail")
                .isEqualTo(ErrorKeys.INVALID_NUMBER_OF_TEAMS);
    }

    @Test
    void getInfoForLobbyShouldSucceed() {
        LobbyCreationDTO lobby = createLobby(4);

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
        LobbyCreationDTO lobby = createLobby(4);

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
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.detail")
                .isEqualTo(ErrorKeys.LOBBY_NOT_FOUND);
    }

    @Test
    void joinLobbyShouldFailForInvalidLobbyCode() {
        restTestClient.post()
                .uri("/api/lobbies/{lobbyCode}/join", "INVALID")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.detail")
                .isEqualTo(ErrorKeys.INVALID_LOBBY_CODE);
    }

    @Test
    void joinLobbyShouldFailForDuplicateTeamLabel() {
        LobbyCreationDTO lobby = createLobby(4);

        LobbyJoinedDTO firstClient = joinLobby(lobby.lobbyCode());
        assignTeam(lobby.lobbyCode(), firstClient.clientId(), "AERO");

        LobbyJoinedDTO secondClient = joinLobby(lobby.lobbyCode());

        restTestClient.post()
                .uri("/api/lobbies/{lobbyCode}/team", lobby.lobbyCode())
                .body(Map.of(
                        "clientId", secondClient.clientId(),
                        "teamLabel", "AERO"
                ))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.detail")
                .isEqualTo(ErrorKeys.TEAM_LABEL_ALREADY_TAKEN);
    }

    @Test
    void assignTeamShouldFailForInvalidLobbyCode() {
        LobbyCreationDTO lobby = createLobby(4);
        LobbyJoinedDTO firstClient = joinLobby(lobby.lobbyCode());

        restTestClient.post()
                .uri("/api/lobbies/{lobbyCode}/team", "INVALID")
                .body(Map.of(
                        "clientId", firstClient.clientId(),
                        "teamLabel", "AERO"
                ))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.detail")
                .isEqualTo(ErrorKeys.INVALID_LOBBY_CODE);
    }


    @Test
    void assignTeamShouldFailForInvalidUuid() {
        LobbyCreationDTO lobby = createLobby(4);

        restTestClient.post()
                .uri("/api/lobbies/{lobbyCode}/team", lobby.lobbyCode())
                .body(Map.of(
                        "clientId", "INVALID_UUID",
                        "teamLabel", "AERO"
                ))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.detail")
                .isEqualTo(ErrorKeys.INVALID_UUID);
    }


    @Test
    void joinLobbyShouldSucceedForDifferentTeamLabels() {
        LobbyCreationDTO lobby = createLobby(4);

        LobbyJoinedDTO firstClient = joinLobby(lobby.lobbyCode());
        assignTeam(lobby.lobbyCode(), firstClient.clientId(), "AERO");

        LobbyJoinedDTO secondClient = joinLobby(lobby.lobbyCode());
        assignTeam(lobby.lobbyCode(), secondClient.clientId(), "GECO");
    }

    @Test
    void joinLobbyShouldFailForUnkownClient() {
        LobbyCreationDTO lobby = createLobby(4);

        LobbyJoinedDTO client = joinLobby(lobby.lobbyCode());
        UUID newClientId = UUID.randomUUID();

        Assertions.assertNotEquals(client.clientId(), newClientId.toString());

        restTestClient.post()
                .uri("/api/lobbies/{lobbyCode}/team", lobby.lobbyCode())
                .body(Map.of(
                        "clientId", newClientId,
                        "teamLabel", "AERO"
                ))
                .exchange()
                .expectStatus().isForbidden()
                .expectBody()
                .jsonPath("$.detail")
                .isEqualTo(ErrorKeys.CLIENT_NOT_IN_LOBBY);
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
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.detail")
                .isEqualTo(ErrorKeys.INVALID_LOBBY_CODE);
    }

    @Test
    void startLobbyShouldFailForInvalidUuid() {
        LobbyCreationDTO lobby = createLobby(4);

        restTestClient.post()
                .uri("/api/lobbies/{lobbyCode}/start", lobby.lobbyCode())
                .body(Map.of("hostId","INVALID_UUID"))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.detail")
                .isEqualTo(ErrorKeys.INVALID_UUID);
    }

    @Test
    void startLobbyShouldFailForDifferentHostId() {
        LobbyCreationDTO lobby = createLobby(4);

        restTestClient.post()
                .uri("/api/lobbies/{lobbyCode}/start", lobby.lobbyCode())
                .body(Map.of("hostId", UUID.randomUUID()))
                .exchange()
                .expectStatus().isForbidden()
                .expectBody()
                .jsonPath("$.detail")
                .isEqualTo(ErrorKeys.ACTION_RESERVED_TO_HOST);
    }

    @Test
    void startGameShouldFailForUnassignedTeam() {
        LobbyCreationDTO lobby = createLobby(4);

        restTestClient.post()
                .uri("/api/lobbies/{lobbyCode}/start", lobby.lobbyCode())
                .body(Map.of("hostId", lobby.hostId()))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.detail")
                .isEqualTo(ErrorKeys.INVALID_TEAM_LABELS);
    }

    /* ====================
       HELPERS
       ==================== */

    private LobbyCreationDTO createLobby(int numberOfTeams) {
        return restTestClient.post()
                .uri("/api/lobbies")
                .body(Map.of("numberOfTeams", numberOfTeams))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(LobbyCreationDTO.class)
                .returnResult()
                .getResponseBody();
    }

    private LobbyJoinedDTO joinLobby(String lobbyCode) {
        return restTestClient.post()
                .uri("/api/lobbies/{lobbyCode}/join", lobbyCode)
                .exchange()
                .expectStatus().isOk()
                .expectBody(LobbyJoinedDTO.class)
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
