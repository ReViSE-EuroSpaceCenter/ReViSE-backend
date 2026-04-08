package be.eurospacecenter.revise.service;

import be.eurospacecenter.revise.exceptions.ErrorKeys;
import be.eurospacecenter.revise.exceptions.InvalidGameStateException;
import be.eurospacecenter.revise.exceptions.NoAutoriseOperationException;
import be.eurospacecenter.revise.exceptions.NotFoundException;
import be.eurospacecenter.revise.model.GameInfo;
import be.eurospacecenter.revise.model.GameState;
import be.eurospacecenter.revise.model.Team;
import be.eurospacecenter.revise.model.lobby.Host;
import be.eurospacecenter.revise.model.lobby.TeamLabel;
import be.eurospacecenter.revise.model.lobbycode.LobbyCode;
import be.eurospacecenter.revise.model.resource.ResourceType;
import be.eurospacecenter.revise.model.resource.TeamsResources;
import be.eurospacecenter.revise.notification.DiscoverNotifier;
import be.eurospacecenter.revise.notification.ResourceNotifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class DiscoverServiceTest {

    private static final LobbyCode VALID_LOBBY_CODE = new LobbyCode("AAAAAA");

    private DiscoverService discoverService;
    private ResourceService resourceService;

    private GameInfo gameInfo;
    private UUID hostId;
    private UUID teamId;

    // ---------------------------------------------------------------------
    // Setup
    // ---------------------------------------------------------------------

    @BeforeEach
    void setUp() {
        discoverService = new DiscoverService(mock(DiscoverNotifier.class));
        resourceService = new ResourceService(mock(ResourceNotifier.class), discoverService);

        hostId = UUID.randomUUID();
        teamId = UUID.randomUUID();

        gameInfo = new GameInfo(new Host(hostId), LocalDateTime.now());
        gameInfo.addTeam(new Team(TeamLabel.EXPE, teamId));
        gameInfo.changeState(GameState.MISSION);
    }

    private void moveGameToResourcePhase() {
        gameInfo.changeState(GameState.RESOURCE);
    }

    // ---------------------------------------------------------------------
    // registerManager
    // ---------------------------------------------------------------------

    @Test
    void shouldRejectRegistrationWhenLobbyCodeIsNull() {
        moveGameToResourcePhase();

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> discoverService.registerManager(null, gameInfo)
        );

        assertEquals(ErrorKeys.INVALID_LOBBY_CODE, ex.getMessage());
    }

    @Test
    void shouldRegisterDiscoverManagerWithValidLobbyCode() {
        moveGameToResourcePhase();

        assertDoesNotThrow(() ->
                discoverService.registerManager(VALID_LOBBY_CODE, gameInfo)
        );
    }

    // ---------------------------------------------------------------------
    // Resource → Discover flow
    // ---------------------------------------------------------------------

    @Test
    void shouldComputeCorrectScoreAfterResourceEncoding() {
        resourceService.registerManager(VALID_LOBBY_CODE, gameInfo);

        resourceService.updateResource(
                VALID_LOBBY_CODE,
                teamId,
                Map.of(ResourceType.ENERGY, 4)
        );

        resourceService.endEncodingResources(VALID_LOBBY_CODE, hostId);

        TeamsResources resources = discoverService.getTeamsResources(VALID_LOBBY_CODE, hostId);

        assertEquals(21, resources.totalScore());
    }

    // ---------------------------------------------------------------------
    // endDiscover
    // ---------------------------------------------------------------------

    @Test
    void shouldEndGameWhenHostIsValid() {
        moveGameToResourcePhase();
        discoverService.registerManager(VALID_LOBBY_CODE, gameInfo);

        assertDoesNotThrow(() ->
                discoverService.endGame(VALID_LOBBY_CODE, hostId)
        );
    }

    @Test
    void shouldFailWhenEndingDiscoverWithUnknownLobbyCode() {
        moveGameToResourcePhase();

        assertThrows(
                NotFoundException.class,
                () -> discoverService.endGame(VALID_LOBBY_CODE, hostId)
        );
    }

    @Test
    void shouldFailWhenEndingDiscoverWithUnknownHost() {
        moveGameToResourcePhase();
        discoverService.registerManager(VALID_LOBBY_CODE, gameInfo);

        UUID unknownHostId = UUID.randomUUID();

        assertThrows(
                NoAutoriseOperationException.class,
                () -> discoverService.endGame(VALID_LOBBY_CODE, unknownHostId)
        );
    }

    @Test
    void shouldFailToGetManagerWhenWrongState() {
        moveGameToResourcePhase();
        discoverService.registerManager(VALID_LOBBY_CODE, gameInfo);

        discoverService.endGame(VALID_LOBBY_CODE, hostId);

        assertThrows(
                InvalidGameStateException.class,
                () -> discoverService.getTeamsResources(VALID_LOBBY_CODE, hostId)
        );
    }
}