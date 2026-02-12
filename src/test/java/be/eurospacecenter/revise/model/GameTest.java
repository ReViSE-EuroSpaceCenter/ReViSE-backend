package be.eurospacecenter.revise.model;

import be.eurospacecenter.revise.exceptions.InvalidGameOperationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class GameTest {

    private Game gameWithOneTeam;
    private Game gameWith4Teams;
    private Game gameWith6Teams;
    private UUID idOfTheLoneTeam;

    @BeforeEach
    void setUp() {
        Host host = new Host(UUID.randomUUID());
        idOfTheLoneTeam = UUID.randomUUID();
        gameWithOneTeam = new Game(host, new ConcurrentHashMap<>(Map.of(idOfTheLoneTeam, new Team(TeamId.EXPE, idOfTheLoneTeam))));
        gameWith4Teams = new Game(host, createTeams("INGE", "MECA", "EXPE", "GECO"));
        gameWith6Teams = new Game(host, createTeams("INGE", "MECA", "EXPE", "GECO", "MEDI", "COOP"));
    }

    @Test
    void gameCreation() {
        assertEquals(100, gameWith4Teams.generalScore());
        assertEquals(150, gameWith6Teams.generalScore());
    }

    @Test
    void removeRessourceToATeam(){
        gameWithOneTeam.removeRessource(idOfTheLoneTeam, ResourceType.ENERGY, 3);
        assertEquals(24, gameWithOneTeam.generalScore());
    }

    @Test
    void removeRessourceToANonExistingTeam(){
        UUID id = UUID.randomUUID();
        assertThrows(InvalidGameOperationException.class, () -> gameWithOneTeam.removeRessource(id, ResourceType.ENERGY, 3));
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