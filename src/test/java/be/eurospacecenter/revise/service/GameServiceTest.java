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

import java.util.UUID;

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

        GameInfo gameInfo = new GameInfo(new Host(UUID.randomUUID()));
        gameInfo.addTeam(new Team(TeamLabel.EXPE, idOfTheLoneTeam));

        gameWithOneTeam = new Game(gameInfo);

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

        gameService.changeTeamMissionState("XXXXXX", idOfTheLoneTeam, MissionType.CLASSIC_1);

        TeamProgression progression = gameService.getGame("XXXXXX").getTeamProgression(idOfTheLoneTeam);

        assertEquals(100f / 7, progression.classicMissionPercentage(), 0.001);
    }

    @Test
    void shouldFailToCompleteTeamMissionWithNonExistingLobbyCode() {
        assertThrows(NotFoundException.class, () -> gameService.changeTeamMissionState("XXXXXX", idOfTheLoneTeam, MissionType.CLASSIC_1));
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

    private GameInfo createTeams(String... labels) {
        GameInfo gameInfo = new GameInfo(new Host(UUID.randomUUID()));

        for (String label : labels) {
            Team team = new Team(TeamLabel.valueOf(label), UUID.randomUUID());

            gameInfo.addTeam(team);
        }

        return gameInfo;
    }
}
