package be.eurospacecenter.revise.model;

import be.eurospacecenter.revise.exceptions.InvalidGameOperationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class TeamTest {

    private Team team;
    private Team teamMeca;
    private UUID id;

    @BeforeEach
    void setUp() {
        id = UUID.randomUUID();

        team = new Team(TeamLabel.AERO, id);
        teamMeca = new Team(TeamLabel.MECA, id);
    }

    @Test
    void teamCreation() {
        assertEquals("AERO", team.getLabel());
        assertEquals(id, team.getClientID());
        assertFalse(team.isMissionBonusCompleted(MissionType.BONUS_1));
        assertFalse(team.isMissionBonusCompleted(MissionType.BONUS_2));
        assertEquals(0, team.getMissionCompletionPercentage());
        assertEquals(25, team.score());
    }

    @ParameterizedTest
    @CsvSource({
            "0,25",
            "10,22"
    })
    void removeEnergyValid(int removed, int expectedScore) {
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
    void removeEnergyInvalid(int removed) {
        assertThrows(IllegalArgumentException.class, () -> team.remove(ResourceType.ENERGY, removed));
    }

    @ParameterizedTest
    @CsvSource({
            "0,25",
            "3,22"
    })
    void removeHumanValid(int removed, int expectedScore) {
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

    @ParameterizedTest
    @EnumSource(MissionType.class)
    void changeMissionStateOnTeam(MissionType missionToComplete) {
        boolean isBonus = missionToComplete.name().startsWith("BONUS");

        if (isBonus) {
            assertFalse(team.isMissionBonusCompleted(missionToComplete));
        } else {
            assertEquals(0, team.getMissionCompletionPercentage());
        }

        if (missionToComplete == MissionType.CLASSIC_8) {
            assertThrows(InvalidGameOperationException.class, () -> team.changeMissionState(missionToComplete));
            return;
        }

        team.changeMissionState(missionToComplete);

        if (isBonus) {
            assertTrue(team.isMissionBonusCompleted(missionToComplete));
        } else {
            assertEquals(100f / 7, team.getMissionCompletionPercentage(), 0.001);
        }
    }

    @ParameterizedTest
    @EnumSource(MissionType.class)
    void changeMissionStateOnMecaTeam(MissionType missionToComplete) {
        boolean isBonus = missionToComplete.name().startsWith("BONUS");

        if (isBonus) {
            assertFalse(teamMeca.isMissionBonusCompleted(missionToComplete));
        } else {
            assertEquals(0, teamMeca.getMissionCompletionPercentage());
        }

        teamMeca.changeMissionState(missionToComplete);

        if (isBonus) {
            assertTrue(teamMeca.isMissionBonusCompleted(missionToComplete));
        } else {
            assertEquals(100f / 8, teamMeca.getMissionCompletionPercentage(), 0.001);
        }
    }

    @Test
    void checkMissionBonusStateWithAClassicMission() {
        assertThrows(IllegalArgumentException.class, () -> team.isMissionBonusCompleted(MissionType.CLASSIC_1));
    }

    @Test
    void changeMissionBonusStateTwice() {
        assertFalse(team.isMissionBonusCompleted(MissionType.BONUS_1));

        team.changeMissionState(MissionType.BONUS_1);
        assertTrue(team.isMissionBonusCompleted(MissionType.BONUS_1));

        team.changeMissionState(MissionType.BONUS_1);
        assertFalse(team.isMissionBonusCompleted(MissionType.BONUS_1));
    }

    @Test
    void changeMissionClassicStateTwice() {
        assertEquals(0, team.getMissionCompletionPercentage());

        team.changeMissionState(MissionType.CLASSIC_1);
        assertEquals((float) 100 / 7, team.getMissionCompletionPercentage(), 0.001);

        team.changeMissionState(MissionType.CLASSIC_1);
        assertEquals(0, team.getMissionCompletionPercentage());
    }
}
