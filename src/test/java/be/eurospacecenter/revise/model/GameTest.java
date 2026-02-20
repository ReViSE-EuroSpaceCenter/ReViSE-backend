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
        gameWith4Teams = new Game(createTeams("AERO", "MECA", "EXPE", "GECO"));
        gameWith6Teams = new Game(createTeams("AERO", "MECA", "EXPE", "GECO", "MEDI", "COOP"));
    }

    @Test
    void gameCreation() {
        TeamProgression progressionOfTheLoneTeam = gameWithOneTeam.getTeamProgression(idOfTheLoneTeam);
        assertEquals(0, progressionOfTheLoneTeam.classicMissionPercentage());
        assertFalse(progressionOfTheLoneTeam.firstBonusMissionCompleted());
        assertFalse(progressionOfTheLoneTeam.secondBonusMissionCompleted());

        assertEquals(25, gameWithOneTeam.generalScore());
        assertEquals(100, gameWith4Teams.generalScore());
        assertEquals(150, gameWith6Teams.generalScore());
    }

    @Test
    void completeTeamClassicMission() {
        gameWithOneTeam.changeTeamMissionState(idOfTheLoneTeam, MissionType.CLASSIC_1);
        TeamProgression progressionOfTheLoneTeam = gameWithOneTeam.getTeamProgression(idOfTheLoneTeam);

        assertEquals(100f / 7, progressionOfTheLoneTeam.classicMissionPercentage(), 0.001);
        assertFalse(progressionOfTheLoneTeam.firstBonusMissionCompleted());
        assertFalse(progressionOfTheLoneTeam.secondBonusMissionCompleted());
    }

    @Test
    void changeTeamFirstBonusMissionState() {
        gameWithOneTeam.changeTeamMissionState(idOfTheLoneTeam, MissionType.BONUS_1);
        TeamProgression progressionOfTheLoneTeam = gameWithOneTeam.getTeamProgression(idOfTheLoneTeam);

        assertEquals(0, progressionOfTheLoneTeam.classicMissionPercentage());
        assertTrue(progressionOfTheLoneTeam.firstBonusMissionCompleted());
        assertFalse(progressionOfTheLoneTeam.secondBonusMissionCompleted());
    }

    @Test
    void changeTeamSecondBonusMissionState() {
        gameWithOneTeam.changeTeamMissionState(idOfTheLoneTeam, MissionType.BONUS_2);
        TeamProgression progressionOfTheLoneTeam = gameWithOneTeam.getTeamProgression(idOfTheLoneTeam);

        assertEquals(0, progressionOfTheLoneTeam.classicMissionPercentage());
        assertFalse(progressionOfTheLoneTeam.firstBonusMissionCompleted());
        assertTrue(progressionOfTheLoneTeam.secondBonusMissionCompleted());
    }

    @Test
    void completeTeamMissionWithInvalidTeam() {
        UUID id = UUID.randomUUID();
        assertThrows(InvalidGameOperationException.class, () -> gameWithOneTeam.changeTeamMissionState(id, MissionType.CLASSIC_1));
    }

    @Test
    void getTeamProgressionWithInvalidTeam() {
        UUID id = UUID.randomUUID();
        assertThrows(InvalidGameOperationException.class, () -> gameWithOneTeam.getTeamProgression(id));
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