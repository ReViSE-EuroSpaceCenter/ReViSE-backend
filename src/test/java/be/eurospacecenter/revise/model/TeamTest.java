package be.eurospacecenter.revise.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class TeamTest {

    private Team team;
    private UUID id;

    @BeforeEach
    void setUp() {
        TeamLabel teamLabel = TeamLabel.INGE;
        id = UUID.randomUUID();

        team = new Team(teamLabel, id);
    }

    @Test
    void teamCreation() {
        assertEquals("INGE", team.getLabel());
        assertEquals(id, team.getClientID());
        assertFalse(team.isMissionCompleted(MissionType.BONUS_1));
        assertFalse(team.isMissionCompleted(MissionType.BONUS_2));
        assertEquals(25, team.score());
    }

    @ParameterizedTest
    @CsvSource({
            "0,25",
            "10,22"
    })
    void removeEnergy_valid(int removed, int expectedScore) {
        team.remove(ResourceType.ENERGY, removed);
        assertEquals(expectedScore, team.score());
    }

    @Test
    void removeEnergySuccessfullyTwice() {
        team.remove(ResourceType.ENERGY, 10);

        assertEquals(22, team.score());

        team.remove(ResourceType.ENERGY, 21);

        assertEquals(15, team.score());
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 41})
    void removeEnergy_invalid(int removed) {
        assertThrows(IllegalArgumentException.class, () -> team.remove(ResourceType.ENERGY, removed));
    }

    @ParameterizedTest
    @CsvSource({
            "0,25",
            "3,22"
    })
    void removeHuman_valid(int removed, int expectedScore) {
        team.remove(ResourceType.HUMAN, removed);
        assertEquals(expectedScore, team.score());
    }

    @Test
    void removeHumanSuccessfullyTwice() {
        team.remove(ResourceType.HUMAN, 3);

        assertEquals(22, team.score());

        team.remove(ResourceType.HUMAN, 2);

        assertEquals(20, team.score());
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 7})
    void removeHuman_invalid(int removed) {
        assertThrows(IllegalArgumentException.class, () -> team.remove(ResourceType.HUMAN, removed));
    }

    @ParameterizedTest
    @CsvSource({
            "0,25",
            "3,22"
    })
    void removeClock_valid(int removed, int expectedScore) {
        team.remove(ResourceType.CLOCK, removed);
        assertEquals(expectedScore, team.score());
    }

    @Test
    void removeClockSuccessfullyTwice() {
        team.remove(ResourceType.CLOCK, 3);

        assertEquals(22, team.score());

        team.remove(ResourceType.CLOCK, 2);

        assertEquals(20, team.score());
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 7})
    void removeClock_invalid(int removed) {
        assertThrows(IllegalArgumentException.class, () -> team.remove(ResourceType.CLOCK, removed));
    }

    @Test
    void completeFirstBonusMission() {
        assertFalse(team.isMissionCompleted(MissionType.BONUS_1));

        team.completeMission(MissionType.BONUS_1);

        assertTrue(team.isMissionCompleted(MissionType.BONUS_1));
        assertFalse(team.isMissionCompleted(MissionType.BONUS_2));
    }

    @Test
    void completeSecondBonusMission() {
        assertFalse(team.isMissionCompleted(MissionType.BONUS_2));

        team.completeMission(MissionType.BONUS_2);

        assertTrue(team.isMissionCompleted(MissionType.BONUS_2));
        assertFalse(team.isMissionCompleted(MissionType.BONUS_1));
    }

}
