package be.eurospacecenter.revise.controller;

import be.eurospacecenter.revise.dto.lobby.LobbyCreationDTO;
import be.eurospacecenter.revise.dto.lobby.LobbyJoinedDTO;
import be.eurospacecenter.revise.exceptions.ErrorKeys;
import be.eurospacecenter.revise.model.lobby.TeamLabel;
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

    // ---------------------------------------------------------------------
    // Tests
    // ---------------------------------------------------------------------

    @Test
    void lobbyCreationValidation() {
        LobbyCreationDTO lobby4 = createLobby(4);
        Assertions.assertNotNull(lobby4.lobbyCode());

        LobbyCreationDTO lobby6 = createLobby(6);
        Assertions.assertNotNull(lobby6.hostId());

        postAndCheck("/api/lobbies", Map.of("numberOfTeams", 3), 400, ErrorKeys.INVALID_NUMBER_OF_TEAMS);
        postAndCheck("/api/lobbies", Map.of("numberOfTeams", 5), 400, ErrorKeys.INVALID_NUMBER_OF_TEAMS);
    }

    @Test
    void getInfoStatusChecks() {
        String code = createLobby(4).lobbyCode();

        restTestClient.get().uri("/api/lobbies/{c}", code).exchange().expectStatus().isOk();
        restTestClient.get().uri("/api/lobbies/INVALID_CODE").exchange().expectStatus().isBadRequest();
    }

    @Test
    void joinLobbyValidation() {
        String code = createLobby(4).lobbyCode();

        restTestClient.post().uri("/api/lobbies/{c}/join", code).exchange().expectStatus().isOk();

        restTestClient.post().uri("/api/lobbies/UNKNOWN/join").exchange().expectStatus().isBadRequest();
        postAndCheck("/api/lobbies/INVALID/join", null, 400, ErrorKeys.INVALID_LOBBY_CODE);
    }

    @Test
    void assignTeamValidation() {
        String code = createLobby(4).lobbyCode();
        String client1 = joinLobby(code).clientId();
        String client2 = joinLobby(code).clientId();

        assignTeam(code, client1);

        String uri = "/api/lobbies/" + code + "/team";
        postAndCheck(uri, Map.of("clientId", client2, "teamLabel", "AERO"), 400, ErrorKeys.TEAM_LABEL_ALREADY_TAKEN);
        postAndCheck(uri, Map.of("clientId", "not-a-uuid", "teamLabel", "GECO"), 400, ErrorKeys.INVALID_UUID);
        postAndCheck(uri, Map.of("clientId", UUID.randomUUID(), "teamLabel", "GECO"), 403, ErrorKeys.CLIENT_NOT_IN_LOBBY);
    }

    @Test
    void startLobbyValidation() {
        LobbyCreationDTO lobby = createLobby(4);
        String uri = "/api/lobbies/" + lobby.lobbyCode() + "/start";

        postAndCheck(uri, Map.of("hostId", UUID.randomUUID()), 403, ErrorKeys.ACTION_RESERVED_TO_HOST);
        postAndCheck(uri, Map.of("hostId", lobby.hostId()), 400, ErrorKeys.INVALID_TEAM_LABELS);
        postAndCheck(uri, Map.of("hostId", "pas-un-uuid"), 400, ErrorKeys.INVALID_UUID);
    }

    @Test
    void invalidStateWhenStartingLobby() {
        LobbyCreationDTO lobby = createLobby(4);
        String uri = "/api/lobbies/" + lobby.lobbyCode() + "/start";

        postAndCheck(uri, Map.of("hostId", lobby.hostId()), 400, ErrorKeys.INVALID_TEAM_LABELS);

        assignTeam(lobby.lobbyCode(), joinLobby(lobby.lobbyCode()).clientId());
        postAndCheck(uri, Map.of("hostId", lobby.hostId()), 400, ErrorKeys.INVALID_TEAM_LABELS);
    }

    // ---------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------

    private void postAndCheck(String uri, Object body, int status, String errorKey) {
        var spec = restTestClient.post().uri(uri);
        if (body != null) spec.body(body);

        spec.exchange()
                .expectStatus().isEqualTo(status)
                .expectBody().jsonPath("$.detail").isEqualTo(errorKey);
    }

    private LobbyCreationDTO createLobby(int teams) {
        return restTestClient.post().uri("/api/lobbies").body(Map.of("numberOfTeams", teams))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(LobbyCreationDTO.class).returnResult().getResponseBody();
    }

    private LobbyJoinedDTO joinLobby(String code) {
        return restTestClient.post().uri("/api/lobbies/{c}/join", code)
                .exchange().expectStatus().isOk()
                .expectBody(LobbyJoinedDTO.class).returnResult().getResponseBody();
    }

    private void assignTeam(String code, String clientId) {
        restTestClient.post().uri("/api/lobbies/{c}/team", code)
                .body(Map.of("clientId", clientId, "teamLabel", TeamLabel.AERO))
                .exchange().expectStatus().isNoContent();
    }
}
