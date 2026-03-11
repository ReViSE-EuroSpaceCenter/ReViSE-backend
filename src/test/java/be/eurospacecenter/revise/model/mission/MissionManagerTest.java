package be.eurospacecenter.revise.model.mission;

import be.eurospacecenter.revise.exceptions.ErrorKeys;
import be.eurospacecenter.revise.exceptions.InvalidMissionOperationException;
import be.eurospacecenter.revise.exceptions.NotFoundException;
import be.eurospacecenter.revise.model.GameInfo;
import be.eurospacecenter.revise.model.lobby.Host;
import be.eurospacecenter.revise.model.lobby.Team;
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

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
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
    void shouldNotAllowNonHostToEndMission() {
        UUID nonHostId = UUID.randomUUID();
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> gameWithOneTeam.endMission(nonHostId)
        );
        assertEquals(ErrorKeys.ACTION_RESERVED_TO_HOST, ex.getMessage());
    }

    @Test
    void shouldAllowHostToEndMissionWhenAllClassicMissionsCompleted() {
        Team team = gameWithOneTeam.getGameInfo().getTeam(idOfTheLoneTeam);

        for (MissionType mission : MissionType.getClassicMissions()) {
            if (mission == MissionType.CLASSIC_8) {
                continue;
            }
            team.changeMissionState(mission);
        }

        assertDoesNotThrow(() -> gameWithOneTeam.endMission(hostId));
    }

    @Test
    void shouldAllowToEndMissionWhenAllClassicMissionsCompleted() {
        UUID mecaTeamId = UUID.randomUUID();

        GameInfo gameInfo = new GameInfo(new Host(hostId), LocalDateTime.now());
        gameInfo.addTeam(new Team(TeamLabel.MECA, mecaTeamId));

        MissionManager missionManager = new MissionManager(gameInfo);

        Team team = missionManager.getGameInfo().getTeam(mecaTeamId);

        for (MissionType mission : MissionType.getClassicMissions()) {
            team.changeMissionState(mission);
        }

        assertDoesNotThrow(() -> missionManager.endMission(hostId));
    }

    @Test
    void shouldNotAllowToEndMissionWhenAtLeastOneClassicMissionIsNotCompleted() {
        Team team = gameWithOneTeam.getGameInfo().getTeam(idOfTheLoneTeam);

        for (MissionType mission : MissionType.getClassicMissions()) {
            if (mission == MissionType.CLASSIC_8) {
                continue;
            }
            team.changeMissionState(mission);
        }

        team.changeMissionState(MissionType.CLASSIC_1);

        InvalidMissionOperationException ex = assertThrows(
                InvalidMissionOperationException.class,
                () -> gameWithOneTeam.endMission(hostId)
        );
        assertEquals(ErrorKeys.LAUNCHER_START_INCOMPLETE_MISSIONS, ex.getMessage());
    }
}