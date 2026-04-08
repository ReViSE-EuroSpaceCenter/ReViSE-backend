package be.eurospacecenter.revise.model.resource;

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

import static be.eurospacecenter.revise.ErrorHelper.assertError;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ResourceManagerTest {

    private ResourceManager manager;
    private final UUID team1 = UUID.randomUUID();
    private final UUID team2 = UUID.randomUUID();
    private final UUID hostId = UUID.randomUUID();
    private GameInfo info;

    // ---------------------------------------------------------------------
    // Setup
    // ---------------------------------------------------------------------

    @BeforeEach
    void setUp() {
        info = new GameInfo(new Host(hostId), LocalDateTime.now());
        info.addTeam(new Team(TeamLabel.EXPE, team1));
        info.addTeam(new Team(TeamLabel.MECA, team2));
        manager = new ResourceManager(info);
    }

    // ---------------------------------------------------------------------
    // Tests
    // ---------------------------------------------------------------------

    @Test
    void initialScoreShouldBeCorrect() {
       assertEquals(33, info.getTeamsResources().totalScore());
    }

    @Test
    void updateResourcesShouldModifyCorrectValues() {
        checkUpdate(team1, Map.of(ResourceType.ENERGY, 3), (21 + 33)/2);
        checkUpdate(team1, Map.of(ResourceType.HUMAN, 2), (17 + 33)/2);
        checkUpdate(team1, Map.of(ResourceType.CLOCK, 1), (4 + 33)/2);
    }

    @Test
    void updateResourcesWithMultipleValuesAndTeams() {
        checkUpdate(team1, Map.of(
                ResourceType.ENERGY, 4,
                ResourceType.HUMAN, 1,
                ResourceType.CLOCK, 3
        ), (5 + 33)/2);

        checkUpdate(team2, Map.of(
                ResourceType.ENERGY, 4,
                ResourceType.HUMAN, 2
        ), (5 + 17)/2);
    }

    @Test
    void securityChecks() {
        assertError(NoAutoriseOperationException.class, ErrorKeys.CLIENT_NOT_IN_LOBBY,
                () -> manager.updateResources(UUID.randomUUID(), Map.of(ResourceType.ENERGY, 1)));

        assertError(NoAutoriseOperationException.class, ErrorKeys.ACTION_RESERVED_TO_HOST,
                () -> manager.ensureHost(UUID.randomUUID()));
    }

    // ---------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------

    private void checkUpdate(UUID id, Map<ResourceType, Integer> deltas, int expectedGlobalScore) {
        manager.updateResources(id, deltas);

        assertEquals(expectedGlobalScore, info.getTeamsResources().totalScore());
    }
}