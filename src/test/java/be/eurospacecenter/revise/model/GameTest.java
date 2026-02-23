package be.eurospacecenter.revise.model;

import be.eurospacecenter.revise.exceptions.ErrorKeys;
import be.eurospacecenter.revise.exceptions.NotFoundException;
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
    private UUID idOfTheLoneTeam;

    @BeforeEach
    void setUp() {
        idOfTheLoneTeam = UUID.randomUUID();
        gameWithOneTeam = new Game(new ConcurrentHashMap<>(Map.of(idOfTheLoneTeam, new Team(TeamLabel.EXPE, idOfTheLoneTeam))));
    }

    @Test
    void gameCreation() {
        TeamProgression progressionOfTheLoneTeam = gameWithOneTeam.getTeamProgression(idOfTheLoneTeam);
        assertEquals(0, progressionOfTheLoneTeam.classicMissionPercentage());
        assertFalse(progressionOfTheLoneTeam.firstBonusMissionCompleted());
        assertFalse(progressionOfTheLoneTeam.secondBonusMissionCompleted());
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
        NotFoundException ex = assertThrows(
                NotFoundException.class,
                () -> gameWithOneTeam.changeTeamMissionState(id, MissionType.CLASSIC_1)
        );
        assertEquals(ErrorKeys.TEAM_NOT_FOUND, ex.getMessage());
    }

    @Test
    void getTeamProgressionWithInvalidTeam() {
        UUID id = UUID.randomUUID();
        NotFoundException ex = assertThrows(
                NotFoundException.class,
                () -> gameWithOneTeam.getTeamProgression(id)
        );
        assertEquals(ErrorKeys.TEAM_NOT_FOUND, ex.getMessage());
    }
}