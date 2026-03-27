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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LauncherManagerTest {

    private LauncherManager gameWithTwoTeam;
    private UUID idOfTheFirstTeam;
    private UUID idOfTheSecondTeam;
    private UUID hostId;

    @BeforeEach
    void setUp() {
        idOfTheFirstTeam = UUID.randomUUID();
        idOfTheSecondTeam = UUID.randomUUID();
        hostId = UUID.randomUUID();

        GameInfo gameInfo = new GameInfo(new Host(hostId), LocalDateTime.now());
        gameInfo.addTeam(new Team(TeamLabel.EXPE, idOfTheFirstTeam));
        gameInfo.addTeam(new Team(TeamLabel.MECA, idOfTheSecondTeam));

        gameWithTwoTeam = new LauncherManager(gameInfo);
    }

    @Test
    void gameCreation() {
        assertEquals(66, gameWithTwoTeam.getTeamsScore(hostId));
    }

    @Test
    void updateResourcesWithEnergy() {
        TeamResources teamResources = gameWithTwoTeam.updateResources(idOfTheFirstTeam, Map.of(ResourceType.ENERGY, 3));
        assertEquals(TeamLabel.EXPE, teamResources.teamLabel());
        assertEquals(ResourceType.ENERGY.getMax()-3, teamResources.resources().get(ResourceType.ENERGY));
        assertEquals(ResourceType.HUMAN.getMax(), teamResources.resources().get(ResourceType.HUMAN));
        assertEquals(ResourceType.CLOCK.getMax(), teamResources.resources().get(ResourceType.CLOCK));
        assertEquals(65, gameWithTwoTeam.getTeamsScore(hostId));
    }

    @Test
    void updateResourcesWithHuman() {
        TeamResources teamResources = gameWithTwoTeam.updateResources(idOfTheFirstTeam, Map.of(ResourceType.HUMAN, 2));
        assertEquals(TeamLabel.EXPE, teamResources.teamLabel());
        assertEquals(ResourceType.ENERGY.getMax(), teamResources.resources().get(ResourceType.ENERGY));
        assertEquals(ResourceType.HUMAN.getMax()-2, teamResources.resources().get(ResourceType.HUMAN));
        assertEquals(ResourceType.CLOCK.getMax(), teamResources.resources().get(ResourceType.CLOCK));
        assertEquals(64, gameWithTwoTeam.getTeamsScore(hostId));
    }

    @Test
    void updateResourcesWithClock() {
        TeamResources teamResources = gameWithTwoTeam.updateResources(idOfTheFirstTeam, Map.of(ResourceType.CLOCK, 1));
        assertEquals(TeamLabel.EXPE, teamResources.teamLabel());
        assertEquals(ResourceType.ENERGY.getMax(), teamResources.resources().get(ResourceType.ENERGY));
        assertEquals(ResourceType.HUMAN.getMax(), teamResources.resources().get(ResourceType.HUMAN));
        assertEquals(ResourceType.CLOCK.getMax()-1, teamResources.resources().get(ResourceType.CLOCK));
        assertEquals(65, gameWithTwoTeam.getTeamsScore(hostId));
    }

    @Test
    void updateResourcesWithAll() {
        TeamResources teamResources = gameWithTwoTeam.updateResources(idOfTheFirstTeam, Map.of(
                ResourceType.ENERGY, 4,
                ResourceType.HUMAN, 1,
                ResourceType.CLOCK, 3
        ));

        assertEquals(TeamLabel.EXPE, teamResources.teamLabel());
        assertEquals(ResourceType.ENERGY.getMax()-4, teamResources.resources().get(ResourceType.ENERGY));
        assertEquals(ResourceType.HUMAN.getMax()-1, teamResources.resources().get(ResourceType.HUMAN));
        assertEquals(ResourceType.CLOCK.getMax()-3, teamResources.resources().get(ResourceType.CLOCK));
        assertEquals(61, gameWithTwoTeam.getTeamsScore(hostId));
    }

    @Test
    void updateResourcesWithMultipleTeam() {
        TeamResources firstTeamResources = gameWithTwoTeam.updateResources(idOfTheFirstTeam, Map.of(
                ResourceType.CLOCK, 3
        ));
        TeamResources secondTeamResources = gameWithTwoTeam.updateResources(idOfTheSecondTeam, Map.of(
                ResourceType.ENERGY, 4,
                ResourceType.HUMAN, 2
        ));

        assertEquals(TeamLabel.EXPE, firstTeamResources.teamLabel());
        assertEquals(ResourceType.ENERGY.getMax(), firstTeamResources.resources().get(ResourceType.ENERGY));
        assertEquals(ResourceType.HUMAN.getMax(), firstTeamResources.resources().get(ResourceType.HUMAN));
        assertEquals(ResourceType.CLOCK.getMax()-3, firstTeamResources.resources().get(ResourceType.CLOCK));

        assertEquals(TeamLabel.MECA, secondTeamResources.teamLabel());
        assertEquals(ResourceType.ENERGY.getMax()-4, secondTeamResources.resources().get(ResourceType.ENERGY));
        assertEquals(ResourceType.HUMAN.getMax()-2, secondTeamResources.resources().get(ResourceType.HUMAN));
        assertEquals(ResourceType.CLOCK.getMax(), secondTeamResources.resources().get(ResourceType.CLOCK));

        assertEquals(60, gameWithTwoTeam.getTeamsScore(hostId));
    }

    @Test
    void updateResourcesWithInvalidTeam() {
        UUID unknownTeamId = UUID.randomUUID();
        Map<ResourceType, Integer> resources = Map.of(ResourceType.ENERGY, 1);

        NoAutoriseOperationException ex = assertThrows(
                NoAutoriseOperationException.class,
                () -> gameWithTwoTeam.updateResources(unknownTeamId, resources)
        );

        assertEquals(ErrorKeys.CLIENT_NOT_IN_LOBBY, ex.getMessage());
    }

    @Test
    void getTeamsScoreWithInvalidHostId() {
        UUID unknownHostId = UUID.randomUUID();

        NoAutoriseOperationException ex = assertThrows(
                NoAutoriseOperationException.class,
                () -> gameWithTwoTeam.getTeamsScore(unknownHostId)
        );

        assertEquals(ErrorKeys.ACTION_RESERVED_TO_HOST, ex.getMessage());
    }

}
