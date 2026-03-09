package be.eurospacecenter.revise.model.launcher;

import be.eurospacecenter.revise.exceptions.ErrorKeys;
import be.eurospacecenter.revise.exceptions.NoAutoriseOperationException;
import be.eurospacecenter.revise.model.GameInfo;
import be.eurospacecenter.revise.model.lobby.Host;
import be.eurospacecenter.revise.model.lobby.Team;
import be.eurospacecenter.revise.model.lobby.TeamLabel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LauncherTest {

    private Launcher gameWithTwoTeam;
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

        gameWithTwoTeam = new Launcher(gameInfo);
    }

    @Test
    void gameCreation() {
        assertEquals(50, gameWithTwoTeam.getGeneralScore(hostId));
    }

    @Test
    void getTeamLabel() {
        assertEquals(TeamLabel.EXPE.name(), gameWithTwoTeam.getTeamLabel(idOfTheFirstTeam));
        assertEquals(TeamLabel.MECA.name(), gameWithTwoTeam.getTeamLabel(idOfTheSecondTeam));
    }

    @Test
    void updateResourcesWithEnergy() {
        gameWithTwoTeam.updateResources(idOfTheFirstTeam, Map.of(ResourceType.ENERGY, 3));
        assertEquals(49, gameWithTwoTeam.getGeneralScore(hostId));
    }

    @Test
    void updateResourcesWithHuman() {
        gameWithTwoTeam.updateResources(idOfTheFirstTeam, Map.of(ResourceType.HUMAN, 2));
        assertEquals(48, gameWithTwoTeam.getGeneralScore(hostId));
    }

    @Test
    void updateResourcesWithClock() {
        gameWithTwoTeam.updateResources(idOfTheFirstTeam, Map.of(ResourceType.CLOCK, 1));
        assertEquals(49, gameWithTwoTeam.getGeneralScore(hostId));
    }

    @Test
    void updateResourcesWithAll() {
        gameWithTwoTeam.updateResources(idOfTheFirstTeam, Map.of(
                ResourceType.ENERGY, 4,
                ResourceType.HUMAN, 1,
                ResourceType.CLOCK, 3
        ));
        assertEquals(45, gameWithTwoTeam.getGeneralScore(hostId));
    }

    @Test
    void updateResourcesWithMultipleTeam() {
        gameWithTwoTeam.updateResources(idOfTheFirstTeam, Map.of(
                ResourceType.CLOCK, 3
        ));
        gameWithTwoTeam.updateResources(idOfTheSecondTeam, Map.of(
                ResourceType.ENERGY, 4,
                ResourceType.HUMAN, 2
        ));
        assertEquals(44, gameWithTwoTeam.getGeneralScore(hostId));
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
    void getGeneralScoreWithInvalidHostId() {
        UUID unknownHostId = UUID.randomUUID();

        NoAutoriseOperationException ex = assertThrows(
                NoAutoriseOperationException.class,
                () -> gameWithTwoTeam.getGeneralScore(unknownHostId)
        );

        assertEquals(ErrorKeys.ACTION_RESERVED_TO_HOST, ex.getMessage());
    }

}
