package be.eurospacecenter.revise.service;

import be.eurospacecenter.revise.dto.response.TeamFullProgressionResponse;
import be.eurospacecenter.revise.dto.response.TeamsProgressionResponse;
import be.eurospacecenter.revise.exceptions.ErrorKeys;
import be.eurospacecenter.revise.exceptions.NotFoundException;
import be.eurospacecenter.revise.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
class GameServiceTest {

    @Autowired
    private GameService gameService;
    private Game gameWithOneTeam;
    private Game gameWith4Teams;
    private Game gameWith6Teams;
    private UUID idOfTheLoneTeam;

    @BeforeEach
    void setUp() {
        gameService.games.clear();
        idOfTheLoneTeam = UUID.randomUUID();
        gameWithOneTeam = new Game(new ConcurrentHashMap<>(Map.of(idOfTheLoneTeam, new Team(TeamLabel.EXPE, idOfTheLoneTeam))));
        gameWith4Teams = new Game(createTeams("AERO", "MECA", "EXPE", "GECO"));
        gameWith6Teams = new Game(createTeams("AERO", "MECA", "EXPE", "GECO", "MEDI", "COOP"));
    }


    @Test
    void shouldRegisterAGameWith4Teams() {
        gameService.registerGame("XXXXXX", gameWith4Teams);

        assertEquals(gameWith4Teams, gameService.getGame("XXXXXX"));
    }

    @Test
    void shouldRegisterAGameWith6Teams() {
        gameService.registerGame("XXXXXX", gameWith6Teams);

        assertEquals(gameWith6Teams, gameService.getGame("XXXXXX"));
    }

    @Test
    void shouldRegisterTwoGames() {
        gameService.registerGame("XXXXXX", gameWith4Teams);
        gameService.registerGame("YYYYYY", gameWith6Teams);

        assertEquals(gameWith4Teams, gameService.getGame("XXXXXX"));
        assertEquals(gameWith6Teams, gameService.getGame("YYYYYY"));
    }

    @Test
    void shouldFailToRegisterGameWithNullLobbyCode() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> gameService.registerGame(null, gameWith4Teams)
        );
        assertEquals(ErrorKeys.INVALID_LOBBY_CODE, ex.getMessage());
    }

    @Test
    void shouldFailToRegisterGameWithEmptyLobbyCode() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> gameService.registerGame("", gameWith4Teams)
        );
        assertEquals(ErrorKeys.INVALID_LOBBY_CODE, ex.getMessage());

        NotFoundException ex2 = assertThrows(NotFoundException.class,
                () -> gameService.getGame("")
        );
        assertEquals(ErrorKeys.GAME_NOT_FOUND, ex2.getMessage());
    }

    @Test
    void shouldCompleteTeamMission() {
        gameService.registerGame("XXXXXX", gameWithOneTeam);

        gameService.changeTeamMissionState("XXXXXX", idOfTheLoneTeam, List.of(MissionType.CLASSIC_1));

        TeamProgression progression = gameService.getGame("XXXXXX").getTeamProgression(idOfTheLoneTeam);

        assertEquals(1, progression.classicMissionsCompleted());
    }

    @Test
    void shouldFailToCompleteTeamMissionWithNonExistingLobbyCode() {
        List<MissionType> missions = List.of(MissionType.CLASSIC_1);
        assertThrows(NotFoundException.class, () -> gameService.changeTeamMissionState("XXXXXX", idOfTheLoneTeam, missions));
    }

    @Test
    void shouldGetTeamFullProgression() {
        gameService.registerGame("XXXXXX", gameWithOneTeam);

        TeamFullProgressionResponse response = gameService.getTeamFullProgression("XXXXXX", idOfTheLoneTeam);

        assertNotNull(response);
        assertEquals(7, response.teamFullProgression().completedMissions().size());
        assertNotNull(response.teamFullProgression().teamProgression());
    }

    @Test
    void shouldGetFourTeamsProgression() {
        gameService.registerGame("XXXXXX", gameWith4Teams);

        TeamsProgressionResponse response = gameService.getTeamsProgression("XXXXXX");

        assertNotNull(response);
        assertEquals(4, response.teamsProgression().size());

        TeamLabel.getAllowedLabels(true).forEach(label -> assertTrue(response.teamsProgression().containsKey(label.name())));
    }

    @Test
    void shouldGetSixTeamsProgression() {
        gameService.registerGame("XXXXXX", gameWith6Teams);

        TeamsProgressionResponse response = gameService.getTeamsProgression("XXXXXX");

        assertNotNull(response);
        assertEquals(6, response.teamsProgression().size());

        TeamLabel.getAllowedLabels(false).forEach(label -> assertTrue(response.teamsProgression().containsKey(label.name())));
    }

    @Test
    void shouldUpdateMultipleMissionsForATeam() {
        gameService.registerGame("XXXXXX", gameWithOneTeam);

        gameService.changeTeamMissionState("XXXXXX", idOfTheLoneTeam, List.of(MissionType.CLASSIC_1, MissionType.CLASSIC_2, MissionType.BONUS_1));

        TeamProgression progression = gameService.getGame("XXXXXX").getTeamProgression(idOfTheLoneTeam);

        assertEquals(2, progression.classicMissionsCompleted());
        assertTrue(progression.firstBonusMissionCompleted());
    }

    @Test
    void shouldUpdateMultipleMissionsForMissionsAlreadyCompleted() {
        gameService.registerGame("XXXXXX", gameWithOneTeam);

        gameService.changeTeamMissionState("XXXXXX", idOfTheLoneTeam, List.of(MissionType.CLASSIC_1, MissionType.CLASSIC_2, MissionType.BONUS_1));

        TeamProgression progression = gameService.getGame("XXXXXX").getTeamProgression(idOfTheLoneTeam);

        assertEquals(2, progression.classicMissionsCompleted());
        assertTrue(progression.firstBonusMissionCompleted());

        gameService.changeTeamMissionState("XXXXXX", idOfTheLoneTeam, List.of(MissionType.CLASSIC_2, MissionType.CLASSIC_3, MissionType.CLASSIC_1, MissionType.CLASSIC_4, MissionType.BONUS_1, MissionType.BONUS_2));

        progression = gameService.getGame("XXXXXX").getTeamProgression(idOfTheLoneTeam);
        assertEquals(2, progression.classicMissionsCompleted());
        assertFalse(progression.firstBonusMissionCompleted());
        assertTrue(progression.secondBonusMissionCompleted());
    }

    private Map<UUID, Team> createTeams(String... labels) {
        Map<UUID, Team> teams = new ConcurrentHashMap<>();
        for (String label : labels) {
            Team team = new Team(TeamLabel.valueOf(label), UUID.randomUUID());
            teams.put(team.getClientID(), team);
        }
        return teams;
    }
}
