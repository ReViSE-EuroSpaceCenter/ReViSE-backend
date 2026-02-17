package be.eurospacecenter.revise.model;

import be.eurospacecenter.revise.exceptions.InvalidGameOperationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class GameTest {

    private Game gameWithOneTeam;
    private Game gameWith4Teams;
    private Game gameWith6Teams;
    private UUID idOfTheLoneTeam;

    @BeforeEach
    void setUp() {
        idOfTheLoneTeam = UUID.randomUUID();
        gameWithOneTeam = new Game(new ConcurrentHashMap<>(Map.of(idOfTheLoneTeam, new Team(TeamLabel.EXPE, idOfTheLoneTeam))));
        gameWith4Teams = new Game(createTeams("INGE", "MECA", "EXPE", "GECO"));
        gameWith6Teams = new Game(createTeams("INGE", "MECA", "EXPE", "GECO", "MEDI", "COOP"));
    }

    @Test
    void gameCreation() {
        assertEquals(25, gameWithOneTeam.generalScore());
        assertEquals(100, gameWith4Teams.generalScore());
        assertEquals(150, gameWith6Teams.generalScore());
    }

    @Test
    void completeTeamMissionWithoutUsingRessources() {
        gameWithOneTeam.completeTeamMission(idOfTheLoneTeam, MissionType.CLASSIC_1, Map.of());

        assertEquals(25, gameWithOneTeam.generalScore());
    }

    @Test
    void completeTeamMissionWithUsingRessources() {
        gameWithOneTeam.completeTeamMission(idOfTheLoneTeam, MissionType.CLASSIC_1, Map.of(ResourceType.ENERGY, 3, ResourceType.HUMAN, 1));
        assertEquals(23, gameWithOneTeam.generalScore());
    }

    @Test
    void completeTeamMissionWithNullRessources() {
        assertThrows(InvalidGameOperationException.class, () -> gameWithOneTeam.completeTeamMission(idOfTheLoneTeam, MissionType.CLASSIC_1, null));
    }

    @Test
    void completeTeamMissionWithUsingTooManyRessources() {
        Map<ResourceType, Integer> resources = Map.of(ResourceType.ENERGY, 50);
        assertThrows(InvalidGameOperationException.class, () -> gameWithOneTeam.completeTeamMission(idOfTheLoneTeam, MissionType.CLASSIC_1, resources));
    }

    @Test
    void completeTeamMissionWithUsingNegativeRessources() {
        Map<ResourceType, Integer> resources = Map.of(ResourceType.ENERGY, -5);
        assertThrows(InvalidGameOperationException.class, () -> gameWithOneTeam.completeTeamMission(idOfTheLoneTeam, MissionType.CLASSIC_1, resources));
    }

    @Test
    void completeTeamMissionWithInvalidTeam() {
        Map<ResourceType, Integer> resources = Map.of(ResourceType.ENERGY, 5);
        UUID id = UUID.randomUUID();
        assertThrows(InvalidGameOperationException.class, () -> gameWithOneTeam.completeTeamMission(id, MissionType.CLASSIC_1, resources));
    }

    private Map<UUID, Team> createTeams(String... labels) {
        Map<UUID, Team> teams = new ConcurrentHashMap<>();
        for (String label : labels) {
            Team team = new Team(TeamLabel.valueOf(label), UUID.randomUUID());
            teams.put(team.getClientID(), team);
        }
        return teams;
    }
}