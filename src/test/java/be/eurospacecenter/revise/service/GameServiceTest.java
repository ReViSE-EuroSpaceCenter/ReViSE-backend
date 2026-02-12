package be.eurospacecenter.revise.service;

import be.eurospacecenter.revise.exceptions.InvalidGameOperationException;
import be.eurospacecenter.revise.exceptions.InvalidStartLobbyException;
import be.eurospacecenter.revise.model.Game;
import be.eurospacecenter.revise.model.Host;
import be.eurospacecenter.revise.model.Team;
import be.eurospacecenter.revise.model.TeamId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;

class GameServiceTest {

    private final GameService gameService = new GameService();
    private Game gameWith4Teams;
    private Game gameWith6Teams;

    @BeforeEach
    void setUp() {
        gameService.games.clear();
        Host host = new Host(UUID.randomUUID());

        gameWith4Teams = new Game(host, createTeams("INGE", "MECA", "EXPE", "GECO"));
        gameWith6Teams = new Game(host, createTeams("INGE", "MECA", "EXPE", "GECO", "MEDI", "COOP"));
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
            Team team = new Team(TeamId.valueOf(label), UUID.randomUUID());
            teams.put(team.getId(), team);
        }
        return teams;
    }
}
