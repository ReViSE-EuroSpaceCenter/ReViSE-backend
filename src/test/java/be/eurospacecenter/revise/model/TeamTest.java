package be.eurospacecenter.revise.model;

import be.eurospacecenter.revise.exceptions.ErrorKeys;
import be.eurospacecenter.revise.exceptions.InvalidGameOperationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class TeamTest {

    private Team team;
    private Team teamMeca;
    private UUID id;
    private TeamProgression progression;

    @BeforeEach
    void setUp() {
        id = UUID.randomUUID();

        team = new Team(TeamLabel.AERO, id);
        progression = team.getProgression();

        teamMeca = new Team(TeamLabel.MECA, id);
    }

    @Test
    void teamCreation() {
        assertEquals("AERO", team.getLabel());
        assertEquals(id, team.getClientID());
        assertInitialProgressionState();
    }

    @ParameterizedTest
    @EnumSource(MissionType.class)
    void changeMissionState_shouldUpdateProgression_correctly(MissionType mission) {
        testMissionForTeamLabel(team, mission);

        progression = teamMeca.getProgression();
        testMissionForTeamLabel(teamMeca, mission);
    }

    @Test
    void changeMissionBonusStateTwice() {
        assertFalse(progression.firstBonusMissionCompleted());

        team.changeMissionState(MissionType.BONUS_1);
        progression = team.getProgression();
        assertTrue(progression.firstBonusMissionCompleted());

        team.changeMissionState(MissionType.BONUS_1);
        progression = team.getProgression();
        assertFalse(progression.firstBonusMissionCompleted());
    }

    @Test
    void changeMissionClassicStateTwice() {
        assertEquals(0, progression.classicMissionPercentage());

        team.changeMissionState(MissionType.CLASSIC_1);
        progression = team.getProgression();
        assertEquals((float) 100 / 7, progression.classicMissionPercentage(), 0.001);

        team.changeMissionState(MissionType.CLASSIC_1);
        progression = team.getProgression();
        assertEquals(0, progression.classicMissionPercentage());
    }

    private void testMissionForTeamLabel(Team team, MissionType mission) {
        if (shouldThrowException(team, mission)) {
            InvalidGameOperationException ex = assertThrows(InvalidGameOperationException.class,
                    () -> team.changeMissionState(mission)
            );
            assertEquals(ErrorKeys.ONLY_MECA_COMPLETE_CLASSIC_8, ex.getMessage());

            return;
        }

        assertInitialProgressionState();
        team.changeMissionState(mission);
        assertProgressionAfterMissionChange(team, mission);
    }

    private boolean shouldThrowException(Team team, MissionType mission) {
        return mission == MissionType.CLASSIC_8
                && !team.getLabel().equals(TeamLabel.MECA.toString());
    }

    private void assertInitialProgressionState() {
        assertFalse(progression.firstBonusMissionCompleted());
        assertFalse(progression.secondBonusMissionCompleted());
        assertEquals(0f, progression.classicMissionPercentage());
    }

    private void assertProgressionAfterMissionChange(Team team, MissionType mission) {
        progression = team.getProgression();

        if (mission == MissionType.BONUS_1) {
            assertTrue(progression.firstBonusMissionCompleted());
        } else if (mission == MissionType.BONUS_2) {
            assertTrue(progression.secondBonusMissionCompleted());
        } else {
            assertClassicMissionPercentage(team);
        }
    }

    private void assertClassicMissionPercentage(Team team) {
        boolean isMeca = team.getLabel().equals(TeamLabel.MECA.toString());
        int classicCount = isMeca ? 8 : 7;
        float expectedPercentage = 100f / classicCount;

        assertEquals(expectedPercentage,
                progression.classicMissionPercentage(),
                0.001);
    }
}
