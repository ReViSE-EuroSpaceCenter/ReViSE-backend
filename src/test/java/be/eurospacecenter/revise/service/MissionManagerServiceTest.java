package be.eurospacecenter.revise.service;

import be.eurospacecenter.revise.dto.response.TeamFullProgressionResponse;
import be.eurospacecenter.revise.dto.response.TeamsProgressionResponse;
import be.eurospacecenter.revise.exceptions.ErrorKeys;
import be.eurospacecenter.revise.exceptions.InvalidMissionOperationException;
import be.eurospacecenter.revise.exceptions.NoAutoriseOperationException;
import be.eurospacecenter.revise.exceptions.NotFoundException;
import be.eurospacecenter.revise.model.*;
import be.eurospacecenter.revise.model.lobby.Host;
import be.eurospacecenter.revise.model.Team;
import be.eurospacecenter.revise.model.mission.MissionType;
import be.eurospacecenter.revise.model.lobby.TeamLabel;
import be.eurospacecenter.revise.model.mission.TeamProgression;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
class MissionManagerServiceTest {

    @Autowired
    private MissionService missionService;
    private GameInfo gameInfoWithOneLoneTeam;
    private GameInfo gameInfoWith4Teams;
    private GameInfo gameInfoWith6Teams;
    private UUID idOfTheLoneTeam;

    @BeforeEach
    void setUp() {
        missionService.managers.clear();
        idOfTheLoneTeam = UUID.randomUUID();

        GameInfo gameInfo = new GameInfo(new Host(UUID.randomUUID()), LocalDateTime.now());
        gameInfo.addTeam(new Team(TeamLabel.EXPE, idOfTheLoneTeam));
        gameInfoWithOneLoneTeam = gameInfo;

        gameInfoWith4Teams = createTeams("AERO", "MECA", "EXPE", "GECO");
        gameInfoWith6Teams = createTeams("AERO", "MECA", "EXPE", "GECO", "MEDI", "COOP");
    }


    @Test
    void shouldRegisterAGameWith4Teams() {
        missionService.registerManager("XXXXXX", gameInfoWith4Teams);

        assertEquals(gameInfoWith4Teams, missionService.getManager("XXXXXX").getGameInfo());
    }

    @Test
    void shouldRegisterAGameWith6Teams() {
        missionService.registerManager("XXXXXX", gameInfoWith6Teams);

        assertEquals(gameInfoWith6Teams, missionService.getManager("XXXXXX").getGameInfo());
    }

    @Test
    void shouldRegisterTwoGames() {
        missionService.registerManager("XXXXXX", gameInfoWith4Teams);
        missionService.registerManager("YYYYYY", gameInfoWith6Teams);

        assertEquals(gameInfoWith4Teams, missionService.getManager("XXXXXX").getGameInfo());
        assertEquals(gameInfoWith6Teams, missionService.getManager("YYYYYY").getGameInfo());
    }

    @Test
    void shouldFailToRegisterGameWithNullLobbyCode() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> missionService.registerManager(null, gameInfoWith4Teams)
        );
        assertEquals(ErrorKeys.INVALID_LOBBY_CODE, ex.getMessage());
    }

    @Test
    void shouldFailToRegisterGameWithEmptyLobbyCode() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> missionService.registerManager("", gameInfoWith4Teams)
        );
        assertEquals(ErrorKeys.INVALID_LOBBY_CODE, ex.getMessage());

        NotFoundException ex2 = assertThrows(NotFoundException.class,
                () -> missionService.getManager("")
        );
        assertEquals(ErrorKeys.MISSION_MANAGER_NOT_FOUND, ex2.getMessage());
    }

    @Test
    void shouldCompleteTeamMission() {
        missionService.registerManager("XXXXXX", gameInfoWithOneLoneTeam);

        missionService.changeTeamMissionsState("XXXXXX", idOfTheLoneTeam, null, List.of(MissionType.CLASSIC_1));

        TeamProgression progression = missionService.getManager("XXXXXX").getTeamProgression(idOfTheLoneTeam);

        assertEquals(1, progression.classicMissionsCompleted());
    }

    @Test
    void shouldFailToCompleteTeamMissionWithNonExistingLobbyCode() {
        List<MissionType> missions = List.of(MissionType.CLASSIC_1);
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> missionService.changeTeamMissionsState("XXXXXX", idOfTheLoneTeam, null, missions)
        );
        assertEquals(ErrorKeys.MISSION_MANAGER_NOT_FOUND, ex.getMessage());

    }

    @Test
    void shouldSucceedToCompleteMissionForHost() {
        UUID hostId = gameInfoWith4Teams.getHostId();

        missionService.registerManager("XXXXXX", gameInfoWith4Teams);

        assertDoesNotThrow(() -> missionService.changeTeamMissionsState("XXXXXX", hostId, "EXPE", List.of(MissionType.CLASSIC_1)));
    }

    @Test
    void shouldNotSucceedToCompleteMissionForUnknownHost() {
        missionService.registerManager("XXXXXX", gameInfoWith4Teams);
        UUID randomHostId = UUID.randomUUID();
        List<MissionType> missions = List.of(MissionType.CLASSIC_1);

        NoAutoriseOperationException ex = assertThrows(NoAutoriseOperationException.class,
                () -> missionService.changeTeamMissionsState("XXXXXX", randomHostId, "EXPE", missions)
        );
        assertEquals(ErrorKeys.ACTION_RESERVED_TO_HOST, ex.getMessage());
    }

    @Test
    void shouldGetTeamFullProgression() {
        missionService.registerManager("XXXXXX", gameInfoWithOneLoneTeam);

        TeamFullProgressionResponse response = missionService.getTeamFullProgression("XXXXXX", idOfTheLoneTeam);

        assertNotNull(response);
        assertEquals(7, response.teamFullProgression().completedMissions().size());
        assertNotNull(response.teamFullProgression().teamProgression());
    }

    @Test
    void shouldGetFourTeamsProgression() {
        missionService.registerManager("XXXXXX", gameInfoWith4Teams);

        TeamsProgressionResponse response = missionService.getTeamsProgression("XXXXXX");

        assertNotNull(response);
        assertEquals(4, response.teamsProgression().size());

        TeamLabel.getAllowedLabels(true).forEach(label -> assertTrue(response.teamsProgression().containsKey(label.name())));
    }

    @Test
    void shouldGetSixTeamsProgression() {
        missionService.registerManager("XXXXXX", gameInfoWith6Teams);

        TeamsProgressionResponse response = missionService.getTeamsProgression("XXXXXX");

        assertNotNull(response);
        assertEquals(6, response.teamsProgression().size());

        TeamLabel.getAllowedLabels(false).forEach(label -> assertTrue(response.teamsProgression().containsKey(label.name())));
    }

    @Test
    void shouldAllowFinishingMission() {
        missionService.registerManager("XXXXXX", gameInfoWithOneLoneTeam);

        for (MissionType missionType : MissionType.getClassicMissions()) {
            if (missionType == MissionType.CLASSIC_8) {
                continue;
            }

            missionService.changeTeamMissionsState("XXXXXX", idOfTheLoneTeam, null, List.of(missionType));
        }

        assertDoesNotThrow(() -> missionService.endMission("XXXXXX", gameInfoWithOneLoneTeam.getHostId()));
    }

    @Test
    void shouldNotAllowNonHostToFinishMission() {
        missionService.registerManager("XXXXXX", gameInfoWithOneLoneTeam);

        UUID nonHostId = UUID.randomUUID();

        NoAutoriseOperationException ex = assertThrows(NoAutoriseOperationException.class,
                () -> missionService.endMission("XXXXXX", nonHostId)
        );
        assertEquals(ErrorKeys.ACTION_RESERVED_TO_HOST, ex.getMessage());
    }

    @Test
    void shouldNotAllowToFinishMissionWhenAtLeastOneClassicMissionIsNotCompleted() {
        missionService.registerManager("XXXXXX", gameInfoWithOneLoneTeam);

        for (MissionType missionType : MissionType.getClassicMissions()) {
            if (missionType == MissionType.CLASSIC_8) {
                continue;
            }

            missionService.changeTeamMissionsState("XXXXXX", idOfTheLoneTeam, null, List.of(missionType));
        }

        missionService.changeTeamMissionsState("XXXXXX", idOfTheLoneTeam, null, List.of(MissionType.CLASSIC_1));

        UUID hostId = gameInfoWithOneLoneTeam.getHostId();

        InvalidMissionOperationException ex = assertThrows(InvalidMissionOperationException.class,
                () -> missionService.endMission("XXXXXX", hostId)
        );
        assertEquals(ErrorKeys.LAUNCHER_START_INCOMPLETE_MISSIONS, ex.getMessage());
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
