package be.eurospacecenter.revise.service;

import be.eurospacecenter.revise.exceptions.ErrorKeys;
import be.eurospacecenter.revise.exceptions.NoAutoriseOperationException;
import be.eurospacecenter.revise.exceptions.NotFoundException;
import be.eurospacecenter.revise.model.GameInfo;
import be.eurospacecenter.revise.model.GameState;
import be.eurospacecenter.revise.model.Team;
import be.eurospacecenter.revise.model.lobby.Host;
import be.eurospacecenter.revise.model.lobby.TeamLabel;
import be.eurospacecenter.revise.model.lobbycode.LobbyCode;
import be.eurospacecenter.revise.model.resource.ResourceType;
import be.eurospacecenter.revise.notification.ResourceNotifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ResourceServiceTest {

    private static final LobbyCode VALID_LOBBY_CODE = new LobbyCode("AAAAAA");

    private ResourceService resourceService;
    private DiscoverService discoverService;

    private GameInfo gameInfo;
    private UUID hostId;
    private UUID teamId;

    // ---------------------------------------------------------------------
    // Setup
    // ---------------------------------------------------------------------

    @BeforeEach
    void setUp() {
        discoverService = mock(DiscoverService.class);
        resourceService = new ResourceService(mock(ResourceNotifier.class), discoverService);

        hostId = UUID.randomUUID();
        teamId = UUID.randomUUID();

        gameInfo = new GameInfo(new Host(hostId), LocalDateTime.now());
        gameInfo.addTeam(new Team(TeamLabel.EXPE, teamId));
        gameInfo.changeState(GameState.MISSION);
        gameInfo.changeState(GameState.LAUNCHER);
    }

    // ---------------------------------------------------------------------
    // registerManager
    // ---------------------------------------------------------------------

    @Test
    void shouldRejectRegistrationWhenLobbyCodeIsNull() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> resourceService.registerManager(null, gameInfo)
        );

        assertEquals(ErrorKeys.INVALID_LOBBY_CODE, ex.getMessage());
    }

    @Test
    void shouldRegisterResourceManagerWithValidLobbyCode() {
        assertDoesNotThrow(() ->
                resourceService.registerManager(VALID_LOBBY_CODE, gameInfo)
        );
    }

    // ---------------------------------------------------------------------
    // updateResource
    // ---------------------------------------------------------------------

    @Test
    void shouldUpdateResourcesForTeam() {
        resourceService.registerManager(VALID_LOBBY_CODE, gameInfo);

        assertDoesNotThrow(() ->
                resourceService.updateResource(
                        VALID_LOBBY_CODE,
                        teamId,
                        Map.of(ResourceType.ENERGY, 4)
                )
        );
    }

    // ---------------------------------------------------------------------
    // endEncodingResources
    // ---------------------------------------------------------------------

    @Test
    void shouldEndResourceEncodingAndNotifyDiscover() {
        resourceService.registerManager(VALID_LOBBY_CODE, gameInfo);

        assertDoesNotThrow(() ->
                resourceService.endEncodingResources(VALID_LOBBY_CODE, hostId)
        );
    }

    @Test
    void shouldFailEndingResourceEncodingWithUnknownLobbyCode() {
        assertThrows(
                NotFoundException.class,
                () -> resourceService.endEncodingResources(VALID_LOBBY_CODE, hostId)
        );

        verify(discoverService, never()).endGame(any(), any());
    }

    @Test
    void shouldFailEndingResourceEncodingWithUnknownHost() {
        resourceService.registerManager(VALID_LOBBY_CODE, gameInfo);
        UUID unknownHostId = UUID.randomUUID();

        assertThrows(
                NoAutoriseOperationException.class,
                () -> resourceService.endEncodingResources(VALID_LOBBY_CODE, unknownHostId)
        );
    }
}