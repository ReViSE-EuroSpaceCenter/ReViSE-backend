package be.eurospacecenter.revise.service;

import be.eurospacecenter.revise.exceptions.ErrorKeys;
import be.eurospacecenter.revise.exceptions.InvalidMissionOperationException;
import be.eurospacecenter.revise.exceptions.NoAutoriseOperationException;
import be.eurospacecenter.revise.exceptions.NotFoundException;
import be.eurospacecenter.revise.model.*;
import be.eurospacecenter.revise.model.lobby.Host;
import be.eurospacecenter.revise.model.Team;
import be.eurospacecenter.revise.model.lobbycode.LobbyCode;
import be.eurospacecenter.revise.model.mission.MissionType;
import be.eurospacecenter.revise.model.lobby.TeamLabel;
import be.eurospacecenter.revise.model.mission.TeamFullProgression;
import be.eurospacecenter.revise.model.mission.TeamProgression;
import be.eurospacecenter.revise.model.mission.TeamsProgression;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.Set;
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

    // ---------------------------------------------------------------------
    // Setup
    // ---------------------------------------------------------------------

    @BeforeEach
    void setUp() {
        missionService.managers.clear();
        idOfTheLoneTeam = UUID.randomUUID();

        GameInfo gameInfo = new GameInfo(new Host(UUID.randomUUID()), LocalDateTime.now());
        gameInfo.addTeam(new Team(TeamLabel.EXPE, idOfTheLoneTeam));
        gameInfoWithOneLoneTeam = gameInfo;

        gameInfoWith4Teams = createTeams(TeamLabel.AERO, TeamLabel.MECA, TeamLabel.EXPE, TeamLabel.GECO);
        gameInfoWith6Teams = createTeams(TeamLabel.AERO, TeamLabel.MECA, TeamLabel.EXPE, TeamLabel.GECO, TeamLabel.MEDI, TeamLabel.COOP);
    }

    // ---------------------------------------------------------------------
    // Tests
    // ---------------------------------------------------------------------

    @Test
    void shouldRegisterAGameWith4Teams() {
        LobbyCode lobbyCode = new LobbyCode("XXXXXX");
        missionService.registerManager(lobbyCode, gameInfoWith4Teams);

        assertEquals(gameInfoWith4Teams, missionService.getManager(lobbyCode).getGameInfo());
    }

    @Test
    void shouldRegisterAGameWith6Teams() {
        LobbyCode lobbyCode = new LobbyCode("XXXXXX");
        missionService.registerManager(lobbyCode, gameInfoWith6Teams);

        LobbyCode lb = new LobbyCode("XXXXXX");

        assertEquals(gameInfoWith6Teams, missionService.getManager(lb).getGameInfo());
    }

    @Test
    void shouldRegisterTwoGames() {
        LobbyCode lobbyCode1 = new LobbyCode("XXXXXX");
        LobbyCode lobbyCode2 = new LobbyCode("YYYYYY");

        missionService.registerManager(lobbyCode1, gameInfoWith4Teams);
        missionService.registerManager(lobbyCode2, gameInfoWith6Teams);

        assertEquals(gameInfoWith4Teams, missionService.getManager(lobbyCode1).getGameInfo());
        assertEquals(gameInfoWith6Teams, missionService.getManager(lobbyCode2).getGameInfo());
    }

    @Test
    void shouldFailToRegisterGameWithNullLobbyCode() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> missionService.registerManager(null, gameInfoWith4Teams)
        );
        assertEquals(ErrorKeys.INVALID_LOBBY_CODE, ex.getMessage());
    }

    @Test
    void shouldCompleteTeamMission() {
        LobbyCode lobbyCode = new LobbyCode("XXXXXX");
        missionService.registerManager(lobbyCode, gameInfoWithOneLoneTeam);

        missionService.changeTeamMissionsState(lobbyCode, idOfTheLoneTeam, null, Set.of(MissionType.CLASSIC_1));

        TeamProgression progression = missionService.getManager(lobbyCode).getTeamProgression(idOfTheLoneTeam);

        assertEquals(1, progression.classicMissionsCompleted());
    }

    @Test
    void shouldFailToCompleteTeamMissionWithNonExistingLobbyCode() {
        LobbyCode lobbyCode = new LobbyCode("XXXXXX");

        Set<MissionType> missions = Set.of(MissionType.CLASSIC_1);
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> missionService.changeTeamMissionsState(lobbyCode, idOfTheLoneTeam, null, missions)
        );
        assertEquals(ErrorKeys.MISSION_MANAGER_NOT_FOUND, ex.getMessage());

    }

    @Test
    void shouldSucceedToCompleteMissionForHost() {
        LobbyCode lobbyCode = new LobbyCode("XXXXXX");

        UUID hostId = gameInfoWith4Teams.getHostId();

        missionService.registerManager(lobbyCode, gameInfoWith4Teams);

        assertDoesNotThrow(() -> missionService.changeTeamMissionsState(lobbyCode, hostId, TeamLabel.EXPE, Set.of(MissionType.CLASSIC_1)));
    }

    @Test
    void shouldNotSucceedToCompleteMissionForUnknownHost() {
        LobbyCode lobbyCode = new LobbyCode("XXXXXX");

        missionService.registerManager(lobbyCode, gameInfoWith4Teams);
        UUID randomHostId = UUID.randomUUID();
        Set<MissionType> missions = Set.of(MissionType.CLASSIC_1);

        NoAutoriseOperationException ex = assertThrows(NoAutoriseOperationException.class,
                () -> missionService.changeTeamMissionsState(lobbyCode, randomHostId, TeamLabel.EXPE, missions)
        );
        assertEquals(ErrorKeys.ACTION_RESERVED_TO_HOST, ex.getMessage());
    }

    @Test
    void shouldGetTeamFullProgression() {
        LobbyCode lobbyCode = new LobbyCode("XXXXXX");

        missionService.registerManager(lobbyCode, gameInfoWithOneLoneTeam);

        TeamFullProgression response = missionService.getTeamFullProgression(lobbyCode, idOfTheLoneTeam);

        assertNotNull(response);
        assertEquals(7, response.completedMissions().size());
        assertNotNull(response.teamProgression());
    }

    @Test
    void shouldGetFourGetTeamsFullProgression() {
        LobbyCode lobbyCode = new LobbyCode("XXXXXX");

        missionService.registerManager(lobbyCode, gameInfoWith4Teams);

        TeamsProgression response = missionService.getTeamsProgression(lobbyCode);

        assertNotNull(response);
        assertEquals(4, response.teamsFullProgression().size());

        TeamLabel.getAllowedLabels(true).forEach(label -> assertTrue(response.teamsFullProgression().containsKey(label)));
    }

    @Test
    void shouldGetSixGetTeamsFullProgression() {
        LobbyCode lobbyCode = new LobbyCode("XXXXXX");

        missionService.registerManager(lobbyCode, gameInfoWith6Teams);

        TeamsProgression response = missionService.getTeamsProgression(lobbyCode);

        assertNotNull(response);
        assertEquals(6, response.teamsFullProgression().size());

        TeamLabel.getAllowedLabels(false).forEach(label -> assertTrue(response.teamsFullProgression().containsKey(label)));
    }

    @Test
    void shouldAllowFinishingMission() {
        LobbyCode lobbyCode = new LobbyCode("XXXXXX");

        missionService.registerManager(lobbyCode, gameInfoWithOneLoneTeam);

        for (MissionType missionType : MissionType.getClassicMissions()) {
            if (missionType == MissionType.CLASSIC_8) {
                continue;
            }

            missionService.changeTeamMissionsState(lobbyCode, idOfTheLoneTeam, null, Set.of(missionType));
        }

        assertDoesNotThrow(() -> missionService.endMission(lobbyCode, gameInfoWithOneLoneTeam.getHostId()));
    }

    @Test
    void shouldNotAllowNonHostToFinishMission() {
        LobbyCode lobbyCode = new LobbyCode("XXXXXX");
        missionService.registerManager(lobbyCode, gameInfoWithOneLoneTeam);

        UUID nonHostId = UUID.randomUUID();

        NoAutoriseOperationException ex = assertThrows(NoAutoriseOperationException.class,
                () -> missionService.endMission(lobbyCode, nonHostId)
        );
        assertEquals(ErrorKeys.ACTION_RESERVED_TO_HOST, ex.getMessage());
    }

    @Test
    void shouldNotAllowToFinishMissionWhenAtLeastOneClassicMissionIsNotCompleted() {
        LobbyCode lobbyCode = new LobbyCode("XXXXXX");

        missionService.registerManager(lobbyCode, gameInfoWithOneLoneTeam);

        for (MissionType missionType : MissionType.getClassicMissions()) {
            if (missionType == MissionType.CLASSIC_8) {
                continue;
            }

            missionService.changeTeamMissionsState(lobbyCode, idOfTheLoneTeam, null, Set.of(missionType));
        }

        missionService.changeTeamMissionsState(lobbyCode, idOfTheLoneTeam, null, Set.of(MissionType.CLASSIC_1));

        UUID hostId = gameInfoWithOneLoneTeam.getHostId();

        InvalidMissionOperationException ex = assertThrows(InvalidMissionOperationException.class,
                () -> missionService.endMission(lobbyCode, hostId)
        );
        assertEquals(ErrorKeys.DISCOVER_START_INCOMPLETE_MISSIONS, ex.getMessage());
    }

    // ---------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------

    private GameInfo createTeams(TeamLabel... labels) {
        GameInfo gameInfo = new GameInfo(new Host(UUID.randomUUID()), LocalDateTime.now());

        for (TeamLabel label : labels) {
            Team team = new Team(label, UUID.randomUUID());

            gameInfo.addTeam(team);
        }

        return gameInfo;
    }
}
