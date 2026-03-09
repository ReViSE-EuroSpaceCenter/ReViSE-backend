package be.eurospacecenter.revise.service;

import be.eurospacecenter.revise.dto.response.ScoreResponse;
import be.eurospacecenter.revise.exceptions.ErrorKeys;
import be.eurospacecenter.revise.exceptions.NotFoundException;
import be.eurospacecenter.revise.model.GameInfo;
import be.eurospacecenter.revise.model.launcher.ResourceType;
import be.eurospacecenter.revise.model.lobby.Host;
import be.eurospacecenter.revise.model.lobby.Team;
import be.eurospacecenter.revise.model.lobby.TeamLabel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
class LauncherServiceTest {

    @Autowired
    private LauncherService launcherService;
    private GameInfo gameInfoWithOneLoneTeam;
    private UUID idOfTheHost;
    private UUID idOfTheLoneTeam;


    @BeforeEach
    void setUp() {
        launcherService.launchers.clear();
        idOfTheLoneTeam = UUID.randomUUID();
        idOfTheHost = UUID.randomUUID();

        GameInfo gameInfo = new GameInfo(new Host(idOfTheHost), LocalDateTime.now());
        gameInfo.addTeam(new Team(TeamLabel.EXPE, idOfTheLoneTeam));
        gameInfoWithOneLoneTeam = gameInfo;
    }

    @Test
    void registerLauncherWithEmptyLobbyCode() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> launcherService.registerLauncher("", gameInfoWithOneLoneTeam)
        );

        assertEquals(ErrorKeys.INVALID_LOBBY_CODE, ex.getMessage());
    }

    @Test
    void registerLauncherWithNullLobbyCode() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> launcherService.registerLauncher(null, gameInfoWithOneLoneTeam)
        );

        assertEquals(ErrorKeys.INVALID_LOBBY_CODE, ex.getMessage());
    }

    @Test
    void registerLauncherWithValidLobbyCode() {
        launcherService.registerLauncher("AAAAAAA", gameInfoWithOneLoneTeam);

        assertEquals(1, launcherService.launchers.size());
    }

    @Test
    void updateResourcesWithInvalidLobbyCode() {
        Map<ResourceType, Integer> resources = Map.of();
        NotFoundException ex = assertThrows(
                NotFoundException.class,
                () -> launcherService.updateResources("INVALID", idOfTheLoneTeam, resources)
        );

        assertEquals(ErrorKeys.LAUNCHER_NOT_FOUND, ex.getMessage());
    }

    @Test
    void updateResourcesWithValidLobbyCode() {
        launcherService.registerLauncher("AAAAAAA", gameInfoWithOneLoneTeam);
        launcherService.updateResources("AAAAAAA", idOfTheLoneTeam, Map.of(ResourceType.ENERGY, 4));

        ScoreResponse scoreResponse = launcherService.getGeneralScore("AAAAAAA", idOfTheHost);

        assertEquals(24, scoreResponse.score());
    }
}
