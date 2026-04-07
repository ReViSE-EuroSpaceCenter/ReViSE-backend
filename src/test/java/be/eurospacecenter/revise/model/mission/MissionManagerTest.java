package be.eurospacecenter.revise.model.mission;

import be.eurospacecenter.revise.exceptions.ErrorKeys;
import be.eurospacecenter.revise.exceptions.InvalidMissionOperationException;
import be.eurospacecenter.revise.exceptions.NoAutoriseOperationException;
import be.eurospacecenter.revise.exceptions.NotFoundException;
import be.eurospacecenter.revise.model.GameInfo;
import be.eurospacecenter.revise.model.Team;
import be.eurospacecenter.revise.model.lobby.Host;
import be.eurospacecenter.revise.model.lobby.TeamLabel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import static be.eurospacecenter.revise.ErrorHelpere.assertError;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class MissionManagerTest {

    private MissionManager manager;
    private final UUID teamId = UUID.randomUUID();
    private final UUID hostId = UUID.randomUUID();

    // ---------------------------------------------------------------------
    // Setup
    // ---------------------------------------------------------------------

    @BeforeEach
    void setUp() {
        GameInfo info = new GameInfo(new Host(hostId), LocalDateTime.now());
        info.addTeam(new Team(TeamLabel.EXPE, teamId));
        manager = new MissionManager(info);
    }

    // ---------------------------------------------------------------------
    // Tests
    // ---------------------------------------------------------------------

    @ParameterizedTest
    @CsvSource({
            "0, false, false, ''",
            "1, false, false, CLASSIC_1",
            "0, true,  false, BONUS_1",
            "0, false, true,  BONUS_2"
    })
    void teamProgressionShouldBeUpdatedCorrectly(int classic, boolean b1, boolean b2, String mission) {
        if (!mission.isBlank()) {
            manager.changeTeamMissionsState(teamId, Set.of(MissionType.valueOf(mission)));
        }

        TeamProgression p = manager.getTeamProgression(teamId);
        assertEquals(classic, p.classicMissionsCompleted());
        assertEquals(b1, p.firstBonusMissionCompleted());
        assertEquals(b2, p.secondBonusMissionCompleted());
    }

    @Test
    void hostCanChangeTeamMissions() {
        TeamProgression p = manager.changeTeamMissionsState(hostId, TeamLabel.EXPE,
                Set.of(MissionType.CLASSIC_1, MissionType.BONUS_1));

        assertEquals(1, p.classicMissionsCompleted());
        assertTrue(p.firstBonusMissionCompleted());
    }

    @Test
    void securityAndNotFoundChecks() {
        assertError(NoAutoriseOperationException.class, ErrorKeys.ACTION_RESERVED_TO_HOST,
                () -> manager.changeTeamMissionsState(UUID.randomUUID(), TeamLabel.EXPE, Set.of()));

        assertError(NotFoundException.class, ErrorKeys.TEAM_NOT_FOUND,
                () -> manager.getTeamProgression(UUID.randomUUID()));

        assertError(InvalidMissionOperationException.class, ErrorKeys.DISCOVER_START_INCOMPLETE_MISSIONS,
                () -> manager.validateEndOfMission(hostId));
    }

    @Test
    void hostCanValidateWhenMissionsAreComplete() {
        Set<MissionType> missions = MissionType.getClassicMissions();
        missions.remove(MissionType.CLASSIC_8);

        manager.changeTeamMissionsState(teamId, missions);

        assertDoesNotThrow(() -> manager.validateEndOfMission(hostId));
    }

    @Test
    void allTeamsMissionsCompletedShouldFollowToggle() {
        Set<MissionType> missions = MissionType.getClassicMissions();
        missions.remove(MissionType.CLASSIC_8);

        manager.changeTeamMissionsState(teamId, missions);

        assertTrue(manager.getTeamsFullProgression().allTeamsMissionsCompleted());

        manager.changeTeamMissionsState(hostId, TeamLabel.EXPE, Set.of(MissionType.CLASSIC_1));
        assertFalse(manager.getTeamsFullProgression().allTeamsMissionsCompleted());
    }
}