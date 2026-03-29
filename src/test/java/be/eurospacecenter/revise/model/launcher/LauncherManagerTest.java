package be.eurospacecenter.revise.model.launcher;

import be.eurospacecenter.revise.exceptions.ErrorKeys;
import be.eurospacecenter.revise.exceptions.NoAutoriseOperationException;
import be.eurospacecenter.revise.model.GameInfo;
import be.eurospacecenter.revise.model.lobby.Host;
import be.eurospacecenter.revise.model.Team;
import be.eurospacecenter.revise.model.lobby.TeamLabel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static be.eurospacecenter.revise.ErrorHelpere.assertError;
import static org.junit.jupiter.api.Assertions.assertEquals;

class LauncherManagerTest {

    private LauncherManager manager;
    private final UUID team1 = UUID.randomUUID();
    private final UUID team2 = UUID.randomUUID();
    private final UUID hostId = UUID.randomUUID();

    // ---------------------------------------------------------------------
    // Setup
    // ---------------------------------------------------------------------

    @BeforeEach
    void setUp() {
        GameInfo info = new GameInfo(new Host(hostId), LocalDateTime.now());
        info.addTeam(new Team(TeamLabel.EXPE, team1));
        info.addTeam(new Team(TeamLabel.MECA, team2));
        manager = new LauncherManager(info);
    }

    // ---------------------------------------------------------------------
    // Tests
    // ---------------------------------------------------------------------

    @Test
    void initialScoreShouldBeCorrect() {
        assertEquals(66, manager.getTeamsScore(hostId));
    }

    @Test
    void updateResourcesShouldModifyCorrectValues() {
        checkUpdate(team1, Map.of(ResourceType.ENERGY, 3), 65);
        checkUpdate(team1, Map.of(ResourceType.HUMAN, 2), 63);
        checkUpdate(team1, Map.of(ResourceType.CLOCK, 1), 62);
    }

    @Test
    void updateResourcesWithMultipleValuesAndTeams() {
        checkUpdate(team1, Map.of(
                ResourceType.ENERGY, 4,
                ResourceType.HUMAN, 1,
                ResourceType.CLOCK, 3
        ), 61);

        checkUpdate(team2, Map.of(
                ResourceType.ENERGY, 4,
                ResourceType.HUMAN, 2
        ), 58);
    }

    @Test
    void securityChecks() {
        assertError(NoAutoriseOperationException.class, ErrorKeys.CLIENT_NOT_IN_LOBBY,
                () -> manager.updateResources(UUID.randomUUID(), Map.of(ResourceType.ENERGY, 1)));

        assertError(NoAutoriseOperationException.class, ErrorKeys.ACTION_RESERVED_TO_HOST,
                () -> manager.getTeamsScore(UUID.randomUUID()));
    }

    // ---------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------

    private void checkUpdate(UUID id, Map<ResourceType, Integer> deltas, int expectedGlobalScore) {
        TeamResources res = manager.updateResources(id, deltas);

        deltas.forEach((type, value) ->
                assertEquals(type.getMax() - value, res.resources().get(type))
        );

        assertEquals(expectedGlobalScore, manager.getTeamsScore(hostId));
    }
}