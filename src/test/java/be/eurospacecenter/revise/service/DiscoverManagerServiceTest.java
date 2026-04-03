package be.eurospacecenter.revise.service;

import be.eurospacecenter.revise.exceptions.ErrorKeys;
import be.eurospacecenter.revise.exceptions.NoAutoriseOperationException;
import be.eurospacecenter.revise.exceptions.NotFoundException;
import be.eurospacecenter.revise.model.GameInfo;
import be.eurospacecenter.revise.model.discover.ResourceType;
import be.eurospacecenter.revise.model.lobby.Host;
import be.eurospacecenter.revise.model.Team;
import be.eurospacecenter.revise.model.lobby.TeamLabel;
import be.eurospacecenter.revise.model.lobbycode.LobbyCode;
import be.eurospacecenter.revise.notification.DiscoverNotifier;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
class DiscoverManagerServiceTest {

    private DiscoverService discoverService;
    private final DiscoverNotifier notifier = mock(DiscoverNotifier.class);

    private GameInfo gameInfoWithOneLoneTeam;
    private UUID idOfTheHost;
    private UUID idOfTheLoneTeam;

    // ---------------------------------------------------------------------
    // Setup
    // ---------------------------------------------------------------------

    @BeforeEach
    void setUp() {
        discoverService = new DiscoverService(notifier);

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

    @Test
    void endDiscoverWithValidLobbyCode() {
        LobbyCode lobbyCode = new LobbyCode("AAAAAA");

        discoverService.registerDiscover(lobbyCode, gameInfoWithOneLoneTeam);

        Assertions.assertDoesNotThrow(() -> discoverService.endDiscover(lobbyCode, idOfTheHost));
    }

    @Test
    void endDiscoverWithInvalidLobbyCode() {
        LobbyCode lobbyCode = new LobbyCode("AAAAAA");

        Assertions.assertThrows(
                NotFoundException.class,
                () -> discoverService.endDiscover(lobbyCode, idOfTheHost)
        );
    }

    @Test
    void endDiscoverWithUnknownHost() {
        LobbyCode lobbyCode = new LobbyCode("AAAAAA");
        UUID newId = UUID.randomUUID();

        discoverService.registerDiscover(lobbyCode, gameInfoWithOneLoneTeam);

        Assertions.assertThrows(
                NoAutoriseOperationException.class,
                () -> discoverService.endDiscover(lobbyCode, newId)
        );
    }


}
