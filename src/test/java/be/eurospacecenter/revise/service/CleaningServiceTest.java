package be.eurospacecenter.revise.service;

import be.eurospacecenter.revise.config.AppMetrics;
import be.eurospacecenter.revise.model.lobby.Host;
import be.eurospacecenter.revise.model.lobby.Lobby;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CleaningServiceTest {

    LobbyService lobbyService;
    MissionService missionService;
    LauncherService launcherService;

    @Mock
    AppMetrics appMetrics;

    @Mock
    MeterRegistry meterRegistry;

    @BeforeEach
    void setup() {
        lobbyService = new LobbyService(missionService, null, appMetrics);
        missionService = new MissionService(null, launcherService);
        launcherService = new LauncherService(null);
    }

    @Test
    void shouldClearOnlyLobby12HoursOld() {
        Map<String, Lobby> shouldBeThere = Map.of(
                "1", new Lobby(new Host(UUID.randomUUID()), 4, LocalDateTime.now()),
                "2", new Lobby(new Host(UUID.randomUUID()), 4, LocalDateTime.now().minusSeconds(1)),
                "3", new Lobby(new Host(UUID.randomUUID()), 4, LocalDateTime.now().minusMinutes(1)),
                "4", new Lobby(new Host(UUID.randomUUID()), 4, LocalDateTime.now().minusMinutes(30)),
                "5", new Lobby(new Host(UUID.randomUUID()), 4, LocalDateTime.now().minusHours(1)),
                "6", new Lobby(new Host(UUID.randomUUID()), 4, LocalDateTime.now().minusHours(6)),
                "7", new Lobby(new Host(UUID.randomUUID()), 4, LocalDateTime.now().minusHours(11))
        );

        Map<String, Lobby> shouldNotBeThere = Map.of(
                "A", new Lobby(new Host(UUID.randomUUID()), 4, LocalDateTime.now().minusHours(12)),
                "B", new Lobby(new Host(UUID.randomUUID()), 4, LocalDateTime.now().minusHours(24)),
                "C", new Lobby(new Host(UUID.randomUUID()), 4, LocalDateTime.now().minusMinutes(36 * 60)),
                "D", new Lobby(new Host(UUID.randomUUID()), 4, LocalDateTime.now().minusDays(1)),
                "E", new Lobby(new Host(UUID.randomUUID()), 4, LocalDateTime.now().minusDays(7)),
                "F", new Lobby(new Host(UUID.randomUUID()), 4, LocalDateTime.now().minusMonths(1))
        );

        shouldBeThere.forEach((k, v) -> lobbyService.addLobby(k, v));
        shouldNotBeThere.forEach((k, v) -> lobbyService.addLobby(k, v));

        shouldBeThere.forEach((k, v) -> missionService.registerManager(k, v.getGameInfo()));
        shouldNotBeThere.forEach((k, v) -> missionService.registerManager(k, v.getGameInfo()));

        shouldBeThere.forEach((k, v) -> launcherService.registerLauncher(k, v.getGameInfo()));
        shouldNotBeThere.forEach((k, v) -> launcherService.registerLauncher(k, v.getGameInfo()));

        CleaningService cleaningService = new CleaningService(
                lobbyService,
                List.of(lobbyService, missionService, launcherService),
                appMetrics
        );

        cleaningService.clearLobbies();

        shouldBeThere.forEach((k, v) -> assertTrue(lobbyService.lobbies.containsKey(k)));
        shouldNotBeThere.forEach((k, v) -> assertFalse(lobbyService.lobbies.containsKey(k)));

        shouldBeThere.forEach((k, v) -> assertTrue(missionService.managers.containsKey(k)));
        shouldNotBeThere.forEach((k, v) -> assertFalse(missionService.managers.containsKey(k)));

        shouldBeThere.forEach((k, v) -> assertTrue(launcherService.launchers.containsKey(k)));
        shouldNotBeThere.forEach((k, v) -> assertFalse(launcherService.launchers.containsKey(k)));
    }
}