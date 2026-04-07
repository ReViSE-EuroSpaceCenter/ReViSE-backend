package be.eurospacecenter.revise.service;

import be.eurospacecenter.revise.model.lobby.Host;
import be.eurospacecenter.revise.model.lobby.Lobby;
import be.eurospacecenter.revise.model.lobbycode.LobbyCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CleaningServiceTest {
    @InjectMocks
    private LobbyService lobbyService;

    @InjectMocks
    private MissionService missionService;

    @InjectMocks
    private DiscoverService discoverService;

    private CleaningService cleaningService;

    // ---------------------------------------------------------------------
    // Setup
    // ---------------------------------------------------------------------

    @BeforeEach
    void setup() {
        cleaningService = new CleaningService(
                lobbyService,
                List.of(lobbyService, missionService, discoverService)
        );
    }

    // ---------------------------------------------------------------------
    // Tests
    // ---------------------------------------------------------------------

    @Test
    void shouldClearOnlyLobby12HoursOld() {
        Map<LobbyCode, Lobby> shouldBeThere = Map.of(
                new LobbyCode("AAAAAA"), new Lobby(new Host(UUID.randomUUID()), 4, LocalDateTime.now()),
                new LobbyCode("AAAAAB"), new Lobby(new Host(UUID.randomUUID()), 4, LocalDateTime.now().minusSeconds(1)),
                new LobbyCode("AAAAAC"), new Lobby(new Host(UUID.randomUUID()), 4, LocalDateTime.now().minusMinutes(1)),
                new LobbyCode("AAAAAD"), new Lobby(new Host(UUID.randomUUID()), 4, LocalDateTime.now().minusMinutes(30)),
                new LobbyCode("AAAAAE"), new Lobby(new Host(UUID.randomUUID()), 4, LocalDateTime.now().minusHours(1)),
                new LobbyCode("AAAAAF"), new Lobby(new Host(UUID.randomUUID()), 4, LocalDateTime.now().minusHours(6)),
                new LobbyCode("AAAAAG"), new Lobby(new Host(UUID.randomUUID()), 4, LocalDateTime.now().minusHours(11))
        );

        Map<LobbyCode, Lobby> shouldNotBeThere = Map.of(
                new LobbyCode("AAAAAH"), new Lobby(new Host(UUID.randomUUID()), 4, LocalDateTime.now().minusHours(12)),
                new LobbyCode("AAAAAI"), new Lobby(new Host(UUID.randomUUID()), 4, LocalDateTime.now().minusHours(24)),
                new LobbyCode("AAAAAJ"), new Lobby(new Host(UUID.randomUUID()), 4, LocalDateTime.now().minusMinutes(36 * 60)),
                new LobbyCode("AAAAAK"), new Lobby(new Host(UUID.randomUUID()), 4, LocalDateTime.now().minusDays(1)),
                new LobbyCode("AAAAAL"), new Lobby(new Host(UUID.randomUUID()), 4, LocalDateTime.now().minusDays(7)),
                new LobbyCode("AAAAAM"), new Lobby(new Host(UUID.randomUUID()), 4, LocalDateTime.now().minusMonths(1))
        );

        lobbyService.managers.putAll(shouldBeThere);
        lobbyService.managers.putAll(shouldNotBeThere);

        shouldBeThere.forEach((k, v) -> missionService.registerManager(k, v.getGameInfo()));
        shouldNotBeThere.forEach((k, v) -> missionService.registerManager(k, v.getGameInfo()));

        shouldBeThere.forEach((k, v) -> discoverService.registerDiscover(k, v.getGameInfo()));
        shouldNotBeThere.forEach((k, v) -> discoverService.registerDiscover(k, v.getGameInfo()));

        cleaningService.clearLobbies();

        shouldBeThere.forEach((k, v) -> assertTrue(lobbyService.managers.containsKey(k), "Should keep " + k));
        shouldNotBeThere.forEach((k, v) -> assertFalse(lobbyService.managers.containsKey(k), "Should remove " + k));

        shouldBeThere.forEach((k, v) -> assertTrue(missionService.managers.containsKey(k), "Manager should still exist for " + k));
        shouldNotBeThere.forEach((k, v) -> assertFalse(missionService.managers.containsKey(k), "Manager should be cleared for " + k));

        shouldBeThere.forEach((k, v) -> assertTrue(discoverService.managers.containsKey(k), "Manager should still exist for " + k));
        shouldNotBeThere.forEach((k, v) -> assertFalse(discoverService.managers.containsKey(k), "Manager should be cleared for " + k));
    }
}