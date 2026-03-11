package be.eurospacecenter.revise.model.mission;

import be.eurospacecenter.revise.exceptions.ErrorKeys;
import be.eurospacecenter.revise.exceptions.InvalidMissionOperationException;
import be.eurospacecenter.revise.exceptions.NoAutoriseOperationException;
import be.eurospacecenter.revise.exceptions.NotFoundException;
import be.eurospacecenter.revise.model.GameInfo;
import be.eurospacecenter.revise.model.lobby.Host;
import be.eurospacecenter.revise.model.Team;
import be.eurospacecenter.revise.model.lobby.TeamLabel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class MissionManagerTest {

    private MissionManager gameWithOneTeam;
    private UUID idOfTheLoneTeam;
    private UUID hostId;

    @BeforeEach
    void setUp() {
        idOfTheLoneTeam = UUID.randomUUID();
        hostId = UUID.randomUUID();

        GameInfo gameInfo = new GameInfo(new Host(hostId), LocalDateTime.now());
        gameInfo.addTeam(new Team(TeamLabel.EXPE, idOfTheLoneTeam));

        gameWithOneTeam = new MissionManager(gameInfo);
    }

    @Test
    void gameCreation() {
        TeamProgression progressionOfTheLoneTeam = gameWithOneTeam.getTeamProgression(idOfTheLoneTeam);
        assertEquals(0, progressionOfTheLoneTeam.classicMissionsCompleted());
        assertFalse(progressionOfTheLoneTeam.firstBonusMissionCompleted());
        assertFalse(progressionOfTheLoneTeam.secondBonusMissionCompleted());
    }

    @Test
    void completeTeamClassicMission() {
        gameWithOneTeam.changeTeamMissionsState(idOfTheLoneTeam, List.of(MissionType.CLASSIC_1));
        TeamProgression progressionOfTheLoneTeam = gameWithOneTeam.getTeamProgression(idOfTheLoneTeam);

        assertEquals(1, progressionOfTheLoneTeam.classicMissionsCompleted());
        assertFalse(progressionOfTheLoneTeam.firstBonusMissionCompleted());
        assertFalse(progressionOfTheLoneTeam.secondBonusMissionCompleted());
    }

    @Test
    void changeTeamFirstBonusMissionState() {
        gameWithOneTeam.changeTeamMissionsState(idOfTheLoneTeam, List.of(MissionType.BONUS_1));
        TeamProgression progressionOfTheLoneTeam = gameWithOneTeam.getTeamProgression(idOfTheLoneTeam);

        assertEquals(0, progressionOfTheLoneTeam.classicMissionsCompleted());
        assertTrue(progressionOfTheLoneTeam.firstBonusMissionCompleted());
        assertFalse(progressionOfTheLoneTeam.secondBonusMissionCompleted());
    }

    @Test
    void changeTeamSecondBonusMissionState() {
        gameWithOneTeam.changeTeamMissionsState(idOfTheLoneTeam, List.of(MissionType.BONUS_2));
        TeamProgression progressionOfTheLoneTeam = gameWithOneTeam.getTeamProgression(idOfTheLoneTeam);

        assertEquals(0, progressionOfTheLoneTeam.classicMissionsCompleted());
        assertFalse(progressionOfTheLoneTeam.firstBonusMissionCompleted());
        assertTrue(progressionOfTheLoneTeam.secondBonusMissionCompleted());
    }

    @Test
    void changeTeamMissionsShouldSucceedForHost() {
        TeamProgression progression = gameWithOneTeam.changeTeamMissionsState(hostId, "EXPE", List.of(MissionType.CLASSIC_1, MissionType.BONUS_1, MissionType.BONUS_2));

        assertEquals(1, progression.classicMissionsCompleted());
        assertTrue(progression.firstBonusMissionCompleted());
        assertTrue(progression.secondBonusMissionCompleted());
    }

    @Test
    void changeTeamMissionsShouldNotSucceedForUnknownHost() {
        UUID unknownHostId = UUID.randomUUID();
        List<MissionType> missions = List.of(MissionType.CLASSIC_1);

        NoAutoriseOperationException ex = assertThrows(
                NoAutoriseOperationException.class,
                () -> gameWithOneTeam.changeTeamMissionsState(unknownHostId, "EXPE", missions)
        );
        assertEquals(ErrorKeys.ACTION_RESERVED_TO_HOST, ex.getMessage());
    }

    @Test
    void changeTeamMissionsShouldNotSucceedForUnknownTeamLabel() {
        String unknownTeamLabel = "UNKNOWN_TEAM";
        List<MissionType> missions = List.of(MissionType.CLASSIC_1);

        NotFoundException ex = assertThrows(
                NotFoundException.class,
                () -> gameWithOneTeam.changeTeamMissionsState(hostId, unknownTeamLabel, missions)
        );
        assertEquals(ErrorKeys.TEAM_NOT_FOUND, ex.getMessage());
    }

    @Test
    void completeTeamMissionWithInvalidTeam() {
        UUID id = UUID.randomUUID();
        List<MissionType> missions = List.of(MissionType.CLASSIC_1);

        NotFoundException ex = assertThrows(
                NotFoundException.class,
                () -> gameWithOneTeam.changeTeamMissionsState(id, missions)
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

    @Test
    void shouldNotAllowNonHostToValidateEndOfMission() {
        UUID nonHostId = UUID.randomUUID();
        NoAutoriseOperationException ex = assertThrows(
                NoAutoriseOperationException.class,
                () -> gameWithOneTeam.validateEndOfMission(nonHostId)
        );
        assertEquals(ErrorKeys.ACTION_RESERVED_TO_HOST, ex.getMessage());
    }

    @Test
    void shouldAllowHostToValidateEndOfMissionWhenAllClassicMissionsCompleted() {
        gameWithOneTeam.changeTeamMissionsState(idOfTheLoneTeam, MissionType.getClassicMissions().stream().toList().subList(0, 7));

        assertDoesNotThrow(() -> gameWithOneTeam.validateEndOfMission(hostId));
    }

    @Test
    void shouldAllowToValidateEndOfMissionWhenAllClassicMissionsCompleted() {
        UUID mecaTeamId = UUID.randomUUID();

        GameInfo gameInfo = new GameInfo(new Host(hostId), LocalDateTime.now());
        gameInfo.addTeam(new Team(TeamLabel.MECA, mecaTeamId));

        MissionManager missionManager = new MissionManager(gameInfo);

        missionManager.changeTeamMissionsState(mecaTeamId, MissionType.getClassicMissions().stream().toList());

        assertDoesNotThrow(() -> missionManager.validateEndOfMission(hostId));
    }

    @Test
    void shouldNotAllowToValidateEndOfMissionWhenAtLeastOneClassicMissionIsNotCompleted() {
        Team team = gameWithOneTeam.getGameInfo().getTeam(idOfTheLoneTeam);

        for (MissionType mission : MissionType.getClassicMissions()) {
            if (mission == MissionType.CLASSIC_8) {
                continue;
            }
            team.updateMission(mission);
        }

        team.updateMission(MissionType.CLASSIC_1);

        InvalidMissionOperationException ex = assertThrows(
                InvalidMissionOperationException.class,
                () -> gameWithOneTeam.validateEndOfMission(hostId)
        );
        assertEquals(ErrorKeys.LAUNCHER_START_INCOMPLETE_MISSIONS, ex.getMessage());
    }

    @Test
    void allMissionsCompletedShouldReturnTrueWhenAllClassicMissionsAreCompleted() {
        GameInfo gameInfo = createTeams("AERO", "EXPE", "GECO", "MECA");
        MissionManager missionManager = new MissionManager(gameInfo);

        missionManager.changeTeamMissionsState(gameInfo.getHostId(), "AERO", MissionType.getClassicMissions().stream().toList().subList(0, 7));
        missionManager.changeTeamMissionsState(gameInfo.getHostId(), "EXPE", MissionType.getClassicMissions().stream().toList().subList(0, 7));
        missionManager.changeTeamMissionsState(gameInfo.getHostId(), "GECO", MissionType.getClassicMissions().stream().toList().subList(0, 7));
        missionManager.changeTeamMissionsState(gameInfo.getHostId(), "MECA", MissionType.getClassicMissions().stream().toList());

        assertTrue(missionManager.isAllTeamsMissionsCompleted());
    }

    @Test
    void allMissionsCompletedShouldReturnFalseWhenAllOtherClassicMissionsAreCompleted() {
        GameInfo gameInfo = createTeams("AERO", "EXPE", "GECO", "MECA");
        MissionManager missionManager = new MissionManager(gameInfo);

        missionManager.changeTeamMissionsState(gameInfo.getHostId(), "AERO", MissionType.getClassicMissions().stream().toList().subList(0, 7));
        missionManager.changeTeamMissionsState(gameInfo.getHostId(), "EXPE", MissionType.getClassicMissions().stream().toList().subList(0, 7));
        missionManager.changeTeamMissionsState(gameInfo.getHostId(), "GECO", MissionType.getClassicMissions().stream().toList().subList(0, 7));

        missionManager.changeTeamMissionsState(gameInfo.getHostId(), "MECA", List.of(MissionType.CLASSIC_1));

        assertFalse(missionManager.isAllTeamsMissionsCompleted());
    }

    @Test
    void allMissionsCompletedShouldReturnFalseWhenAllNotOtherClassicMissionsAreCompleted() {
        GameInfo gameInfo = createTeams("AERO", "EXPE", "GECO", "MECA");
        MissionManager missionManager = new MissionManager(gameInfo);

        missionManager.changeTeamMissionsState(gameInfo.getHostId(), "AERO", MissionType.getClassicMissions().stream().toList().subList(0, 7));

        assertFalse(missionManager.isAllTeamsMissionsCompleted());
    }

    @Test
    void allMissionsCompletedShouldReturnFalseWhenAllOnIsIncomplete() {
        GameInfo gameInfo = createTeams("AERO", "EXPE", "GECO", "MECA");
        MissionManager missionManager = new MissionManager(gameInfo);

        missionManager.changeTeamMissionsState(gameInfo.getHostId(), "AERO", MissionType.getClassicMissions().stream().toList().subList(0, 7));
        missionManager.changeTeamMissionsState(gameInfo.getHostId(), "EXPE", MissionType.getClassicMissions().stream().toList().subList(0, 7));
        missionManager.changeTeamMissionsState(gameInfo.getHostId(), "GECO", MissionType.getClassicMissions().stream().toList().subList(0, 7));
        missionManager.changeTeamMissionsState(gameInfo.getHostId(), "MECA", MissionType.getClassicMissions().stream().toList());

        missionManager.changeTeamMissionsState(gameInfo.getHostId(), "MECA", List.of(MissionType.CLASSIC_1));

        assertFalse(missionManager.isAllTeamsMissionsCompleted());
    }

    @Test
    void allMissionsCompletedShouldReturnFalseWhenOnceIsOnAndOff() {
        GameInfo gameInfo = createTeams("AERO", "EXPE", "GECO", "MECA");
        MissionManager missionManager = new MissionManager(gameInfo);

        missionManager.changeTeamMissionsState(gameInfo.getHostId(), "AERO", MissionType.getClassicMissions().stream().toList().subList(0, 7));
        missionManager.changeTeamMissionsState(gameInfo.getHostId(), "EXPE", MissionType.getClassicMissions().stream().toList().subList(0, 7));
        missionManager.changeTeamMissionsState(gameInfo.getHostId(), "GECO", MissionType.getClassicMissions().stream().toList().subList(0, 7));
        missionManager.changeTeamMissionsState(gameInfo.getHostId(), "MECA", MissionType.getClassicMissions().stream().toList());

        missionManager.changeTeamMissionsState(gameInfo.getHostId(), "MECA", List.of(MissionType.CLASSIC_1));
        missionManager.changeTeamMissionsState(gameInfo.getHostId(), "MECA", List.of(MissionType.CLASSIC_1));

        assertTrue(missionManager.isAllTeamsMissionsCompleted());
    }

    private GameInfo createTeams(String... labels) {
        GameInfo gameInfo = new GameInfo(new Host(UUID.randomUUID()), LocalDateTime.now());

        for (String label : labels) {
            Team team = new Team(TeamLabel.valueOf(label), UUID.randomUUID());

            gameInfo.addTeam(team);
        }

        return gameInfo;
    }
}