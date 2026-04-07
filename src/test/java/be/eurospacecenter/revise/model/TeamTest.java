package be.eurospacecenter.revise.model;

import be.eurospacecenter.revise.exceptions.ErrorKeys;
import be.eurospacecenter.revise.exceptions.InvalidDiscoverOperationException;
import be.eurospacecenter.revise.exceptions.InvalidMissionOperationException;
import be.eurospacecenter.revise.model.resource.ResourceType;
import be.eurospacecenter.revise.model.lobby.TeamLabel;
import be.eurospacecenter.revise.model.mission.MissionType;
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
    private final UUID id = UUID.randomUUID();

    // ---------------------------------------------------------------------
    // Setup
    // ---------------------------------------------------------------------

    @BeforeEach
    void setUp() {
        team = new Team(TeamLabel.AERO, id);
        teamMeca = new Team(TeamLabel.MECA, id);
    }

    // ---------------------------------------------------------------------
    // Tests
    // ---------------------------------------------------------------------

    @Test
    void teamCreation() {
        assertEquals(TeamLabel.AERO, team.getLabel());
        assertEquals(id, team.getClientID());

        TeamProgression p = team.getProgression();
        assertEquals(0, p.classicMissionsCompleted());
        assertFalse(p.firstBonusMissionCompleted());
    }

    @Test
    void missionStateToggleLogic() {
        team.updateMission(MissionType.BONUS_1);
        assertTrue(team.getProgression().firstBonusMissionCompleted());
        team.updateMission(MissionType.BONUS_1);
        assertFalse(team.getProgression().firstBonusMissionCompleted());

        team.updateMission(MissionType.CLASSIC_1);
        assertEquals(1, team.getProgression().classicMissionsCompleted());
        team.updateMission(MissionType.CLASSIC_1);
        assertEquals(0, team.getProgression().classicMissionsCompleted());
    }

    @Test
    void mission8IsReservedToMeca() {
        assertDoesNotThrow(() -> teamMeca.updateMission(MissionType.CLASSIC_8));

        InvalidMissionOperationException ex = assertThrows(InvalidMissionOperationException.class,
                () -> team.updateMission(MissionType.CLASSIC_8));
        assertEquals(ErrorKeys.ONLY_MECA_COMPLETE_CLASSIC_8, ex.getMessage());
    }

    @Test
    void allClassicMissionsCompletedValidation() {
        for (MissionType m : MissionType.getClassicMissions()) {
            if (m != MissionType.CLASSIC_8) team.updateMission(m);
        }
        assertTrue(team.allClassicMissionsCompleted());
        assertEquals(7, team.getFullProgression().completedMissions().size());

        for (MissionType m : MissionType.getClassicMissions()) {
            teamMeca.updateMission(m);
        }
        assertTrue(teamMeca.allClassicMissionsCompleted());
        assertEquals(8, teamMeca.getFullProgression().completedMissions().size());
    }

    @ParameterizedTest
    @EnumSource(ResourceType.class)
    void resourceLimitsValidation(ResourceType type) {
        assertDoesNotThrow(() -> team.updateResources(Map.of(type, type.getMax())));
    }

    @ParameterizedTest
    @EnumSource(ResourceType.class)
    void resourceOverLimitsValidation(ResourceType type) {
        assertResourceError(Map.of(type, type.getMax() + 1));
    }

    // ---------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------

    private void assertResourceError(Map<ResourceType, Integer> resources) {
        InvalidDiscoverOperationException ex = assertThrows(
                InvalidDiscoverOperationException.class,
                () -> team.updateResources(resources)
        );
        assertEquals(ErrorKeys.INSUFFICIENT_RESOURCES, ex.getMessage());
    }
}