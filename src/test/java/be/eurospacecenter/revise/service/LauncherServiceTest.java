package be.eurospacecenter.revise.service;

import be.eurospacecenter.revise.exceptions.NoAutoriseOperationException;
import be.eurospacecenter.revise.exceptions.NotFoundException;
import be.eurospacecenter.revise.model.GameInfo;
import be.eurospacecenter.revise.model.GameState;
import be.eurospacecenter.revise.model.lobby.Host;
import be.eurospacecenter.revise.model.lobbycode.LobbyCode;
import be.eurospacecenter.revise.notification.LauncherNotifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class LauncherServiceTest {

    private static final LobbyCode VALID_LOBBY_CODE = new LobbyCode("AAAAAA");

    private LauncherService launcherService;

    private GameInfo gameInfo;
    private UUID hostId;

    // ---------------------------------------------------------------------
    // Setup
    // ---------------------------------------------------------------------

    @BeforeEach
    void setUp() {
        launcherService = new LauncherService(mock(LauncherNotifier.class), mock(ResourceService.class));

        hostId = UUID.randomUUID();

        gameInfo = new GameInfo(new Host(hostId), LocalDateTime.now());
        gameInfo.changeState(GameState.MISSION);
    }

    @Test
    void shouldGetTeamsProgressionWithValidLobbyCode() {
        launcherService.registerManager(VALID_LOBBY_CODE, gameInfo);

        assertDoesNotThrow(() ->
                launcherService.getTeamsProgression(VALID_LOBBY_CODE)
        );
    }

    @Test
    void shouldFailToGetTeamsProgressionWithUnknownLobbyCode() {
        assertThrows(
                NotFoundException.class,
                () -> launcherService.getTeamsProgression(VALID_LOBBY_CODE)
        );
    }

    @Test
    void shouldFailToRegisterManagerWithNullLobbyCode() {
        assertThrows(
                IllegalArgumentException.class,
                () -> launcherService.registerManager(null, gameInfo)
        );
    }

    @Test
    void shouldStartResourceEncodingWithValidHost() {
        launcherService.registerManager(VALID_LOBBY_CODE, gameInfo);

        assertDoesNotThrow(() ->
                launcherService.endLauncher(VALID_LOBBY_CODE, hostId)
        );
    }

    @Test
    void shouldFailToStartResourceEncodingWithUnknownLobbyCode() {
        assertThrows(
                NotFoundException.class,
                () -> launcherService.endLauncher(VALID_LOBBY_CODE, hostId)
        );
    }

    @Test
    void shouldFailToStartResourceEncodingWithUnknownHost() {
        launcherService.registerManager(VALID_LOBBY_CODE, gameInfo);

        UUID unknownHostId = UUID.randomUUID();

        assertThrows(
                NoAutoriseOperationException.class,
                () -> launcherService.endLauncher(VALID_LOBBY_CODE, unknownHostId)
        );
    }
}
