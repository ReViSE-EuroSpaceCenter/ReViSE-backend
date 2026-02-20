package be.eurospacecenter.revise.service;

import be.eurospacecenter.revise.exceptions.InvalidGameOperationException;
import be.eurospacecenter.revise.exceptions.InvalidStartLobbyException;
import be.eurospacecenter.revise.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;

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
        assertThrows(InvalidStartLobbyException.class, () -> gameService.registerGame(null, gameWith4Teams));
    }

    @Test
    void shouldFailToRegisterGameWithEmptyLobbyCode() {
        assertThrows(InvalidStartLobbyException.class, () -> gameService.registerGame("", gameWith4Teams));
        assertNull(gameService.getGame(""));
    }

    @Test
    void shouldCompleteTeamMission() {
        gameService.registerGame("XXXXXX", gameWithOneTeam);

        gameService.changeATeamMissionState("XXXXXX", idOfTheLoneTeam, MissionType.CLASSIC_1);

        TeamProgression progression = gameService.getGame("XXXXXX").getTeamProgression(idOfTheLoneTeam);

        assertEquals(100f / 7, progression.classicMissionPercentage(), 0.001);
    }

    @Test
    void shouldFailToCompleteTeamMissionWithNonExistingLobbyCode() {
        assertThrows(InvalidGameOperationException.class, () -> gameService.changeATeamMissionState("XXXXXX", idOfTheLoneTeam, MissionType.CLASSIC_1));
    }

    @Test
    void shouldGetGeneralScoreGame() {
        gameService.registerGame("XXXXXX", gameWith4Teams);
        gameService.registerGame("YYYYYY", gameWith6Teams);

        assertEquals(100, gameService.getGeneralScore("XXXXXX"));
        assertEquals(150, gameService.getGeneralScore("YYYYYY"));
    }

    @Test
    void shouldFailToGetGeneralScoreGameWithNonExistingLobbyCode() {
        assertThrows(InvalidGameOperationException.class, () -> gameService.getGeneralScore("XXXXXX"));
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
