package be.eurospacecenter.revise.controller;

import be.eurospacecenter.revise.exceptions.ErrorKeys;
import be.eurospacecenter.revise.model.lobby.LobbyJoined;
import be.eurospacecenter.revise.model.lobby.TeamLabel;
import be.eurospacecenter.revise.model.lobbycode.LobbyCode;
import be.eurospacecenter.revise.model.mission.MissionType;
import be.eurospacecenter.revise.service.LobbyService;
import be.eurospacecenter.revise.service.MissionService;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
class ResourceControllerTest {

    private static final String BASE_URI = "/api/resources";
    private static final Map<String, Integer> VALID_RESOURCES = Map.of("ENERGY", 1, "HUMAN", 2, "CLOCK", 3);

    @Autowired
    private RestTestClient restTestClient;

    @Autowired
    private LobbyService lobbyService;

    @Autowired
    private MissionService missionService;

    private LobbyCode lobbyCode;
    private UUID hostId;
    private UUID validClientId;

    // ---------------------------------------------------------------------
    // Setup
    // ---------------------------------------------------------------------

    @BeforeEach
    void setUp() {
        createLobbyAndHost();
        joinClientAndStartGame();
        completeMissionsAndEndGame();
    }

    // ---------------------------------------------------------------------
    // Tests
    // ---------------------------------------------------------------------

    @Test
    void startResourceEncoding_shouldSucceed() {
        restTestClient.put().uri(BASE_URI + "/" + lobbyCode.lobbyCode() + "/start").body(Map.of("hostId", hostId.toString())).exchange().expectStatus().isNoContent();
    }

    @Test
    void startResourceEncoding_shouldFail_withInvalidLobbyCode() {
        restTestClient.put().uri(BASE_URI + "/INVALID/start").body(Map.of("hostId", hostId.toString())).exchange().expectStatus().isBadRequest().expectBody().jsonPath("$.detail").isEqualTo(ErrorKeys.INVALID_LOBBY_CODE);
    }

    @Test
    void startResourceEncoding_shouldFail_withInvalidHost() {
        restTestClient.put().uri(BASE_URI + "/" + lobbyCode.lobbyCode() + "/start").body(Map.of("hostId", UUID.randomUUID().toString())).exchange().expectStatus().isForbidden().expectBody().jsonPath("$.detail").isEqualTo(ErrorKeys.ACTION_RESERVED_TO_HOST);
    }

    @Test
    void updateResources_shouldSucceed() {
        updateResources(lobbyCode.lobbyCode(), validClientId, VALID_RESOURCES).expectStatus().isNoContent();
    }

    @Test
    void updateResources_shouldFail_withInvalidLobbyCode() {
        updateResources("INVALID", validClientId, VALID_RESOURCES).expectStatus().isBadRequest().expectBody().jsonPath("$.detail").isEqualTo(ErrorKeys.INVALID_LOBBY_CODE);
    }

    @Test
    void updateResources_shouldFail_withInvalidClient() {
        updateResources(lobbyCode.lobbyCode(), UUID.randomUUID(), VALID_RESOURCES).expectStatus().isForbidden().expectBody().jsonPath("$.detail").isEqualTo(ErrorKeys.CLIENT_NOT_IN_LOBBY);
    }

    @Test
    void updateResources_shouldFail_withInvalidResource() {
        updateResources(lobbyCode.lobbyCode(), validClientId, Map.of("WOOD", 1)).expectStatus().isBadRequest().expectBody().jsonPath("$.detail").isEqualTo(ErrorKeys.INVALID_RESOURCE_TYPE);
    }

    @Test
    void endResourceEncoding_shouldSucceed() {
        restTestClient.put().uri(BASE_URI + "/" + lobbyCode.lobbyCode() + "/end").body(Map.of("hostId", hostId.toString())).exchange().expectStatus().isNoContent();
    }

    @Test
    void endResourceEncoding_shouldFail_withInvalidLobbyCode() {
        restTestClient.put().uri(BASE_URI + "/INVALID/end").body(Map.of("hostId", hostId.toString())).exchange().expectStatus().isBadRequest().expectBody().jsonPath("$.detail").isEqualTo(ErrorKeys.INVALID_LOBBY_CODE);
    }

    @Test
    void endResourceEncoding_shouldFail_withInvalidHost() {
        restTestClient.put().uri(BASE_URI + "/" + lobbyCode.lobbyCode() + "/end").body(Map.of("hostId", UUID.randomUUID().toString())).exchange().expectStatus().isForbidden().expectBody().jsonPath("$.detail").isEqualTo(ErrorKeys.ACTION_RESERVED_TO_HOST);
    }

    @Test
    void illegalStateShouldBeReturnedWhenTryingJoinLobby() {
        restTestClient.post()
                .uri("/api/lobbies/" + lobbyCode.lobbyCode() + "/join")
                .exchange().expectStatus()
                .is4xxClientError()
                .expectBody().jsonPath("$.error").isEqualTo(ErrorKeys.INVALID_GAME_STATE);
    }

    @Test
    void illegalStateShouldBeReturnedWhenTryingStartGame() {
        restTestClient.post()
                .uri("/api/lobbies/" + lobbyCode.lobbyCode() + "/start")
                .body(Map.of("hostId", hostId.toString()))
                .exchange().expectStatus()
                .is4xxClientError()
                .expectBody().jsonPath("$.error").isEqualTo(ErrorKeys.INVALID_GAME_STATE);
    }

    @Test
    void illegalStateShouldBeReturnedWhenTryingChangeMissionsState() {
        restTestClient.put()
                .uri("/api/missions/" + lobbyCode.lobbyCode())
                .body(Map.of("id", hostId.toString(), "teamLabel", TeamLabel.EXPE.name(), "updateMissions", Set.of("CLASSIC_1")))
                .exchange().expectStatus()
                .is4xxClientError()
                .expectBody().jsonPath("$.error").isEqualTo(ErrorKeys.INVALID_GAME_STATE);
    }

    // ---------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------

    private void createLobbyAndHost() {
        var responseBody = restTestClient.post().uri("/api/lobbies").body(Map.of("numberOfTeams", 4)).exchange().expectStatus().isCreated().expectBody(String.class).returnResult().getResponseBody();

        assertNotNull(responseBody);

        lobbyCode = new LobbyCode(JsonPath.read(responseBody, "$.lobbyCode"));
        hostId = UUID.fromString(JsonPath.read(responseBody, "$.hostId"));
    }

    private void joinClientAndStartGame() {
        TeamLabel.getAllowedLabels(true).forEach(teamLabel -> {
            LobbyJoined joined = lobbyService.joinLobby(lobbyCode);
            validClientId = UUID.fromString(joined.clientId());

            lobbyService.assignTeam(lobbyCode, validClientId, teamLabel);
        });
        lobbyService.startGame(lobbyCode, hostId);
    }

    private void completeMissionsAndEndGame() {
        TeamLabel.getAllowedLabels(true).forEach(teamLabel -> {
            Set<MissionType> missions = MissionType.getClassicMissions();
            if (teamLabel != TeamLabel.MECA) {
                missions.remove(MissionType.CLASSIC_8);
            }
            missionService.changeTeamMissionsState(lobbyCode, hostId, teamLabel, missions);
        });
        missionService.endMission(lobbyCode, hostId);
    }

    private RestTestClient.ResponseSpec updateResources(String lobby, UUID clientId, Map<String, Integer> resources) {
        return restTestClient.post().uri(BASE_URI + "/" + lobby).body(Map.of("clientId", clientId.toString(), "resources", resources)).exchange();
    }
}