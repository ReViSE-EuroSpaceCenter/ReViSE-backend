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

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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
        gameWithOneTeam.changeTeamMissionsState(idOfTheLoneTeam, Set.of(MissionType.CLASSIC_1));
        TeamProgression progressionOfTheLoneTeam = gameWithOneTeam.getTeamProgression(idOfTheLoneTeam);

        assertEquals(1, progressionOfTheLoneTeam.classicMissionsCompleted());
        assertFalse(progressionOfTheLoneTeam.firstBonusMissionCompleted());
        assertFalse(progressionOfTheLoneTeam.secondBonusMissionCompleted());
    }

    @Test
    void changeTeamFirstBonusMissionState() {
        gameWithOneTeam.changeTeamMissionsState(idOfTheLoneTeam, Set.of(MissionType.BONUS_1));
        TeamProgression progressionOfTheLoneTeam = gameWithOneTeam.getTeamProgression(idOfTheLoneTeam);

        assertEquals(0, progressionOfTheLoneTeam.classicMissionsCompleted());
        assertTrue(progressionOfTheLoneTeam.firstBonusMissionCompleted());
        assertFalse(progressionOfTheLoneTeam.secondBonusMissionCompleted());
    }

    @Test
    void changeTeamSecondBonusMissionState() {
        gameWithOneTeam.changeTeamMissionsState(idOfTheLoneTeam, Set.of(MissionType.BONUS_2));
        TeamProgression progressionOfTheLoneTeam = gameWithOneTeam.getTeamProgression(idOfTheLoneTeam);

        assertEquals(0, progressionOfTheLoneTeam.classicMissionsCompleted());
        assertFalse(progressionOfTheLoneTeam.firstBonusMissionCompleted());
        assertTrue(progressionOfTheLoneTeam.secondBonusMissionCompleted());
    }

    @Test
    void changeTeamMissionsShouldSucceedForHost() {
        TeamProgression progression = gameWithOneTeam.changeTeamMissionsState(hostId, TeamLabel.EXPE, Set.of(MissionType.CLASSIC_1, MissionType.BONUS_1, MissionType.BONUS_2));

        assertEquals(1, progression.classicMissionsCompleted());
        assertTrue(progression.firstBonusMissionCompleted());
        assertTrue(progression.secondBonusMissionCompleted());
    }

    @Test
    void changeTeamMissionsShouldNotSucceedForUnknownHost() {
        UUID unknownHostId = UUID.randomUUID();
        Set<MissionType> missions = Set.of(MissionType.CLASSIC_1);

        NoAutoriseOperationException ex = assertThrows(
                NoAutoriseOperationException.class,
                () -> gameWithOneTeam.changeTeamMissionsState(unknownHostId, TeamLabel.EXPE, missions)
        );
        assertEquals(ErrorKeys.ACTION_RESERVED_TO_HOST, ex.getMessage());
    }

    @Test
    void completeTeamMissionWithInvalidTeam() {
        UUID id = UUID.randomUUID();
        Set<MissionType> missions = Set.of(MissionType.CLASSIC_1);

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
        gameWithOneTeam.changeTeamMissionsState(idOfTheLoneTeam, MissionType.getClassicMissions().stream().limit(7).collect(Collectors.toSet()));

        assertDoesNotThrow(() -> gameWithOneTeam.validateEndOfMission(hostId));
    }

    @Test
    void shouldAllowToValidateEndOfMissionWhenAllClassicMissionsCompleted() {
        UUID mecaTeamId = UUID.randomUUID();

        GameInfo gameInfo = new GameInfo(new Host(hostId), LocalDateTime.now());
        gameInfo.addTeam(new Team(TeamLabel.MECA, mecaTeamId));

        MissionManager missionManager = new MissionManager(gameInfo);

        missionManager.changeTeamMissionsState(mecaTeamId, MissionType.getClassicMissions());

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
        GameInfo gameInfo = createTeams(TeamLabel.AERO, TeamLabel.EXPE, TeamLabel.GECO, TeamLabel.MECA);
        MissionManager missionManager = new MissionManager(gameInfo);

        missionManager.changeTeamMissionsState(gameInfo.getHostId(), TeamLabel.AERO, MissionType.getClassicMissions().stream().limit(7).collect(Collectors.toSet()));
        missionManager.changeTeamMissionsState(gameInfo.getHostId(), TeamLabel.EXPE, MissionType.getClassicMissions().stream().limit(7).collect(Collectors.toSet()));
        missionManager.changeTeamMissionsState(gameInfo.getHostId(), TeamLabel.GECO, MissionType.getClassicMissions().stream().limit(7).collect(Collectors.toSet()));
        missionManager.changeTeamMissionsState(gameInfo.getHostId(), TeamLabel.MECA, MissionType.getClassicMissions());

        assertTrue(missionManager.getTeamsFullProgression().allTeamsMissionsCompleted());
    }

    @Test
    void allMissionsCompletedShouldReturnFalseWhenAllOtherClassicMissionsAreCompleted() {
        GameInfo gameInfo = createTeams(TeamLabel.AERO, TeamLabel.EXPE, TeamLabel.GECO, TeamLabel.MECA);
        MissionManager missionManager = new MissionManager(gameInfo);

        missionManager.changeTeamMissionsState(gameInfo.getHostId(), TeamLabel.AERO, MissionType.getClassicMissions().stream().limit(7).collect(Collectors.toSet()));
        missionManager.changeTeamMissionsState(gameInfo.getHostId(), TeamLabel.EXPE, MissionType.getClassicMissions().stream().limit(7).collect(Collectors.toSet()));
        missionManager.changeTeamMissionsState(gameInfo.getHostId(), TeamLabel.GECO, MissionType.getClassicMissions().stream().limit(7).collect(Collectors.toSet()));

        missionManager.changeTeamMissionsState(gameInfo.getHostId(), TeamLabel.MECA, Set.of(MissionType.CLASSIC_1));

        assertFalse(missionManager.getTeamsFullProgression().allTeamsMissionsCompleted());
    }

    @Test
    void allMissionsCompletedShouldReturnFalseWhenAllNotOtherClassicMissionsAreCompleted() {
        GameInfo gameInfo = createTeams(TeamLabel.AERO, TeamLabel.EXPE, TeamLabel.GECO, TeamLabel.MECA);
        MissionManager missionManager = new MissionManager(gameInfo);

        missionManager.changeTeamMissionsState(gameInfo.getHostId(), TeamLabel.AERO, MissionType.getClassicMissions().stream().limit(7).collect(Collectors.toSet()));

        assertFalse(missionManager.getTeamsFullProgression().allTeamsMissionsCompleted());
    }

    @Test
    void allMissionsCompletedShouldReturnFalseWhenAllOnIsIncomplete() {
        GameInfo gameInfo = createTeams(TeamLabel.AERO, TeamLabel.EXPE, TeamLabel.GECO, TeamLabel.MECA);
        MissionManager missionManager = new MissionManager(gameInfo);

        missionManager.changeTeamMissionsState(gameInfo.getHostId(), TeamLabel.AERO, MissionType.getClassicMissions().stream().limit(7).collect(Collectors.toSet()));
        missionManager.changeTeamMissionsState(gameInfo.getHostId(), TeamLabel.EXPE, MissionType.getClassicMissions().stream().limit(7).collect(Collectors.toSet()));
        missionManager.changeTeamMissionsState(gameInfo.getHostId(), TeamLabel.GECO, MissionType.getClassicMissions().stream().limit(7).collect(Collectors.toSet()));
        missionManager.changeTeamMissionsState(gameInfo.getHostId(), TeamLabel.MECA, MissionType.getClassicMissions());

        missionManager.changeTeamMissionsState(gameInfo.getHostId(), TeamLabel.MECA, Set.of(MissionType.CLASSIC_1));

        assertFalse(missionManager.getTeamsFullProgression().allTeamsMissionsCompleted());
    }

    @Test
    void allMissionsCompletedShouldReturnFalseWhenOnceIsOnAndOff() {
        GameInfo gameInfo = createTeams(TeamLabel.AERO, TeamLabel.EXPE, TeamLabel.GECO, TeamLabel.MECA);
        MissionManager missionManager = new MissionManager(gameInfo);

        missionManager.changeTeamMissionsState(gameInfo.getHostId(), TeamLabel.AERO, MissionType.getClassicMissions().stream().limit(7).collect(Collectors.toSet()));
        missionManager.changeTeamMissionsState(gameInfo.getHostId(), TeamLabel.EXPE, MissionType.getClassicMissions().stream().limit(7).collect(Collectors.toSet()));
        missionManager.changeTeamMissionsState(gameInfo.getHostId(), TeamLabel.GECO, MissionType.getClassicMissions().stream().limit(7).collect(Collectors.toSet()));
        missionManager.changeTeamMissionsState(gameInfo.getHostId(), TeamLabel.MECA, MissionType.getClassicMissions());

        missionManager.changeTeamMissionsState(gameInfo.getHostId(), TeamLabel.MECA, Set.of(MissionType.CLASSIC_1));
        missionManager.changeTeamMissionsState(gameInfo.getHostId(), TeamLabel.MECA, Set.of(MissionType.CLASSIC_1));

        assertTrue(missionManager.getTeamsFullProgression().allTeamsMissionsCompleted());
    }

    private GameInfo createTeams(TeamLabel... labels) {
        GameInfo gameInfo = new GameInfo(new Host(UUID.randomUUID()), LocalDateTime.now());

        for (TeamLabel label : labels) {
            Team team = new Team(label, UUID.randomUUID());

            gameInfo.addTeam(team);
        }

        return gameInfo;
    }
}