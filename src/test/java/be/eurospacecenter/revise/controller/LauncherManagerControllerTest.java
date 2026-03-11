package be.eurospacecenter.revise.controller;

import be.eurospacecenter.revise.dto.response.LobbyJoinedResponse;
import be.eurospacecenter.revise.exceptions.ErrorKeys;
import be.eurospacecenter.revise.model.lobby.TeamLabel;
import be.eurospacecenter.revise.model.mission.MissionType;
import be.eurospacecenter.revise.service.LobbyService;
import be.eurospacecenter.revise.service.MissionService;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.util.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
class LauncherManagerControllerTest {

    @Autowired
    private RestTestClient restTestClient;

    @Autowired
    private LobbyService lobbyService;

    @Autowired
    private MissionService missionService;

    private String lobbyCode;
    private Map<UUID, TeamLabel> teams = new HashMap<>();
    private UUID hostId;

    @BeforeEach
    void setUp() {
        var result = restTestClient.post()
                .uri("/api/lobbies")
                .body(Map.of("numberOfTeams", 4))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .returnResult();


        Assertions.assertNotNull(result.getResponseBody());

        String body = new String(result.getResponseBody());

        Assertions.assertNotNull(body);

        lobbyCode = JsonPath.read(body, "$.lobbyCode");
        hostId = UUID.fromString(JsonPath.read(body, "$.hostId"));

        List<TeamLabel> labels = TeamLabel.getAllowedLabels(true).stream().toList();

        for (int i = 0; i < 4; i++) {
            LobbyJoinedResponse response = lobbyService.joinLobby(lobbyCode);
            teams.put(UUID.fromString(response.clientId()), labels.get(i));
            lobbyService.assignTeam(lobbyCode, UUID.fromString(response.clientId()), labels.get(i).toString());
        }

        lobbyService.startGame(lobbyCode, hostId);


        for (Map.Entry<UUID, TeamLabel> entry : teams.entrySet()) {
            UUID teamId = entry.getKey();
            TeamLabel label = entry.getValue();

            List<MissionType> missionsToComplete = new ArrayList<>(MissionType.getClassicMissions());

            if (label != TeamLabel.MECA) {
                missionsToComplete.removeLast();
            }

            missionService.changeTeamMissionsState(lobbyCode, teamId, null, missionsToComplete);
        }

        missionService.endMission(lobbyCode, hostId);
    }

    @Test
    void updateRessourceShouldSucceed() {
        restTestClient.put()
                .uri("/api/launchers/" + lobbyCode)
                .body(Map.of(
                        "clientId", teams.keySet().iterator().next().toString(),
                        "resources", Map.of("ENERGY", 1, "HUMAN", 2, "CLOCK", 3)
                ))
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void updateRessourceShouldFailWithInvalidLobbyCode() {
        restTestClient.put()
                .uri("/api/launchers/INVALID")
                .body(Map.of(
                        "clientId", teams.keySet().iterator().next().toString(),
                        "resources", Map.of("ENERGY", 1, "HUMAN", 2, "CLOCK", 3)
                ))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.detail")
                .isEqualTo(ErrorKeys.INVALID_LOBBY_CODE);
    }

    @Test
    void updateRessourceShouldFailWithInvalidUuid() {
        restTestClient.put()
                .uri("/api/launchers/" + lobbyCode)
                .body(Map.of(
                        "clientId", UUID.randomUUID().toString(),
                        "resources", Map.of("ENERGY", 1, "HUMAN", 2, "CLOCK", 3)
                ))
                .exchange()
                .expectStatus().isForbidden()
                .expectBody()
                .jsonPath("$.detail")
                .isEqualTo(ErrorKeys.CLIENT_NOT_IN_LOBBY);
    }

    @Test
    void updateRessourceShouldFailWithInvalidResource() {
        restTestClient.put()
                .uri("/api/launchers/" + lobbyCode)
                .body(Map.of(
                        "clientId", teams.keySet().iterator().next().toString(),
                        "resources", Map.of("WOOD", 1)
                ))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.detail")
                .isEqualTo(ErrorKeys.INVALID_RESOURCE_TYPE);
    }

    @Test
    void getScoreShouldSucceed() {
        restTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/launchers/{lobbyCode}/score")
                        .queryParam("hostId", hostId.toString())
                        .build(lobbyCode))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.score")
                .isNumber();
    }

    @Test
    void getScoreShouldFailWithInvalidLobbyCode() {
        restTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/launchers/{lobbyCode}/score")
                        .queryParam("hostId", hostId.toString())
                        .build("INVALID"))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.detail")
                .isEqualTo(ErrorKeys.INVALID_LOBBY_CODE);
    }

    @Test
    void getScoreShouldFailWithInvalidUuid() {
        restTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/launchers/{lobbyCode}/score")
                        .queryParam("hostId", UUID.randomUUID().toString())
                        .build(lobbyCode))
                .exchange()
                .expectStatus().isForbidden()
                .expectBody()
                .jsonPath("$.detail")
                .isEqualTo(ErrorKeys.ACTION_RESERVED_TO_HOST);
    }
}