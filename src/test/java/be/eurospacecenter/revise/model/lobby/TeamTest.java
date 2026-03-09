package be.eurospacecenter.revise.model.lobby;

import be.eurospacecenter.revise.exceptions.ErrorKeys;
import be.eurospacecenter.revise.exceptions.InvalidLauncherOperationException;
import be.eurospacecenter.revise.exceptions.InvalidMissionOperationException;
import be.eurospacecenter.revise.model.launcher.ResourceType;
import be.eurospacecenter.revise.model.mission.MissionType;
import be.eurospacecenter.revise.model.mission.TeamFullProgression;
import be.eurospacecenter.revise.model.mission.TeamProgression;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;
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
        assertEquals(0, progression.classicMissionsCompleted());

        team.changeMissionState(MissionType.CLASSIC_1);
        progression = team.getProgression();
        assertEquals(1, progression.classicMissionsCompleted(), 1);

        team.changeMissionState(MissionType.CLASSIC_1);
        progression = team.getProgression();
        assertEquals(0, progression.classicMissionsCompleted());
    }

    @Test
    void teamFullMissionProgressionsShouldBe7Missions() {
        TeamFullProgression prog = team.getFullProgression();

        assertEquals(7, prog.completedMissions().size());
        assertFalse(prog.completedMissions().containsKey(MissionType.CLASSIC_8.name()));
    }

    @Test
    void teamFullMissionProgressionsShouldBe8MissionsForMeca() {
        TeamFullProgression prog = teamMeca.getFullProgression();

        assertEquals(8, prog.completedMissions().size());
        assertTrue(prog.completedMissions().containsKey(MissionType.CLASSIC_8.name()));
    }

    @Test
    void allClassicMissionsCompleted() {
        assertFalse(team.allClassicMissionsCompleted());

        for (MissionType mission : MissionType.getClassicMissions()) {
            if (mission == MissionType.CLASSIC_8) {
                continue;
            }
            team.changeMissionState(mission);
        }

        assertTrue(team.allClassicMissionsCompleted());
    }

    @Test
    void allClassicMissionsCompletedForMeca() {
        assertFalse(teamMeca.allClassicMissionsCompleted());

        for (MissionType mission : MissionType.getClassicMissions()) {
            teamMeca.changeMissionState(mission);
        }

        assertTrue(teamMeca.allClassicMissionsCompleted());
    }

    @Test
    void shouldHaveMultipleMissionsCompleted() {
        team.changeMissionState(MissionType.CLASSIC_1);
        team.changeMissionState(MissionType.CLASSIC_2);
        team.changeMissionState(MissionType.BONUS_1);

        progression = team.getProgression();
        assertEquals(2, progression.classicMissionsCompleted());
        assertTrue(progression.firstBonusMissionCompleted());
        assertFalse(progression.secondBonusMissionCompleted());
    }


    @ParameterizedTest
    @EnumSource(ResourceType.class)
    void removeResourceOverLimitOnce(ResourceType resourceType) {
        Map<ResourceType, Integer> resourcesToRemove = Map.of(resourceType, resourceType.getMax()+1);
        InvalidLauncherOperationException ex = assertThrows(
                InvalidLauncherOperationException.class,
                () -> team.removeResources(resourcesToRemove)
        );

        assertEquals(ErrorKeys.INSUFFICIENT_RESOURCES, ex.getMessage());
    }

    @ParameterizedTest
    @EnumSource(ResourceType.class)
    void removeResourceOverLimitTwice(ResourceType resourceType) {
        team.removeResources(Map.of(resourceType, resourceType.getMax()));
        Map<ResourceType, Integer> resourcesToRemove = Map.of(resourceType, 1);
        InvalidLauncherOperationException ex = assertThrows(
                InvalidLauncherOperationException.class,
                () -> team.removeResources(resourcesToRemove)
        );

        assertEquals(ErrorKeys.INSUFFICIENT_RESOURCES, ex.getMessage());
    }

    private void testMissionForTeamLabel(Team team, MissionType mission) {
        if (shouldThrowException(team, mission)) {
            InvalidMissionOperationException ex = assertThrows(InvalidMissionOperationException.class, () -> team.changeMissionState(mission));
            assertEquals(ErrorKeys.ONLY_MECA_COMPLETE_CLASSIC_8, ex.getMessage());

            return;
        }

        assertInitialProgressionState();
        team.changeMissionState(mission);
        assertProgressionAfterMissionChange(team, mission);
    }

    private boolean shouldThrowException(Team team, MissionType mission) {
        return mission == MissionType.CLASSIC_8 && !team.getLabel().equals(TeamLabel.MECA.toString());
    }

    private void assertInitialProgressionState() {
        assertFalse(progression.firstBonusMissionCompleted());
        assertFalse(progression.secondBonusMissionCompleted());
        assertEquals(0, progression.classicMissionsCompleted());
    }

    private void assertProgressionAfterMissionChange(Team team, MissionType mission) {
        progression = team.getProgression();

        if (mission == MissionType.BONUS_1) {
            assertTrue(progression.firstBonusMissionCompleted());
        } else if (mission == MissionType.BONUS_2) {
            assertTrue(progression.secondBonusMissionCompleted());
        } else {
            assertEquals(1, team.getProgression().classicMissionsCompleted());
        }
    }
}
