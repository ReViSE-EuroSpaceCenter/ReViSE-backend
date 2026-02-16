package be.eurospacecenter.revise.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class GameTest {

    private Game gameWith4Teams;
    private Game gameWith6Teams;

    @BeforeEach
    void setUp() {
        gameWith4Teams = new Game(createTeams("INGE", "MECA", "EXPE", "GECO"));
        gameWith6Teams = new Game(createTeams("INGE", "MECA", "EXPE", "GECO", "MEDI", "COOP"));
    }

    @Test
    void gameCreation() {
        assertEquals(100, gameWith4Teams.generalScore());
        assertEquals(150, gameWith6Teams.generalScore());
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