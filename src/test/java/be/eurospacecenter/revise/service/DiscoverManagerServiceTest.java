package be.eurospacecenter.revise.service;

import be.eurospacecenter.revise.exceptions.ErrorKeys;
import be.eurospacecenter.revise.model.GameInfo;
import be.eurospacecenter.revise.model.discover.ResourceType;
import be.eurospacecenter.revise.model.lobby.Host;
import be.eurospacecenter.revise.model.Team;
import be.eurospacecenter.revise.model.lobby.TeamLabel;
import be.eurospacecenter.revise.model.lobbycode.LobbyCode;
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
class DiscoverManagerServiceTest {

    @Autowired
    private DiscoverService discoverService;
    private GameInfo gameInfoWithOneLoneTeam;
    private UUID idOfTheHost;
    private UUID idOfTheLoneTeam;

    // ---------------------------------------------------------------------
    // Setup
    // ---------------------------------------------------------------------

    @BeforeEach
    void setUp() {
        discoverService.managers.clear();
        idOfTheLoneTeam = UUID.randomUUID();
        idOfTheHost = UUID.randomUUID();

        GameInfo gameInfo = new GameInfo(new Host(idOfTheHost), LocalDateTime.now());
        gameInfo.addTeam(new Team(TeamLabel.EXPE, idOfTheLoneTeam));
        gameInfoWithOneLoneTeam = gameInfo;
    }

    // ---------------------------------------------------------------------
    // Tests
    // ---------------------------------------------------------------------

    @Test
    void registerDiscoverWithNullLobbyCode() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> discoverService.registerDiscover(null, gameInfoWithOneLoneTeam)
        );

        assertEquals(ErrorKeys.INVALID_LOBBY_CODE, ex.getMessage());
    }

    @Test
    void registerDiscoverWithValidLobbyCode() {
        LobbyCode lobbyCode = new LobbyCode("AAAAAA");


        discoverService.registerDiscover(lobbyCode, gameInfoWithOneLoneTeam);

        assertEquals(1, discoverService.managers.size());
    }

    @Test
    void updateResourcesWithValidLobbyCode() {
        LobbyCode lobbyCode = new LobbyCode("AAAAAA");

        discoverService.registerDiscover(lobbyCode, gameInfoWithOneLoneTeam);
        discoverService.updateResources(lobbyCode, idOfTheLoneTeam, Map.of(ResourceType.ENERGY, 4));

        int score = discoverService.getTeamsScore(lobbyCode, idOfTheHost);

        assertEquals(21, score);
    }
}
