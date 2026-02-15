package be.eurospacecenter.revise.model;

import be.eurospacecenter.revise.exceptions.InvalidStartLobbyException;
import be.eurospacecenter.revise.exceptions.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LobbyTest {

    private final Host host = new Host(UUID.randomUUID());
    private final UUID hostId = host.id();

    private final UUID team1Id = UUID.randomUUID();
    private final UUID team2Id = UUID.randomUUID();
    private final UUID team3Id = UUID.randomUUID();
    private final UUID team4Id = UUID.randomUUID();
    private final UUID team5Id = UUID.randomUUID();
    private final UUID team6Id = UUID.randomUUID();

    private final Lobby lobby4Teams = new Lobby(host, 4);
    private final Lobby lobby6Teams = new Lobby(host, 6);

    @BeforeEach
    void setUp() {
        lobby4Teams.addTeam(new Team(team1Id));
        lobby4Teams.addTeam(new Team(team2Id));
        lobby4Teams.addTeam(new Team(team3Id));
        lobby4Teams.addTeam(new Team(team4Id));

        lobby6Teams.addTeam(new Team(team1Id));
        lobby6Teams.addTeam(new Team(team2Id));
        lobby6Teams.addTeam(new Team(team3Id));
        lobby6Teams.addTeam(new Team(team4Id));
        lobby6Teams.addTeam(new Team(team5Id));
        lobby6Teams.addTeam(new Team(team6Id));

    }

    @Test
    void shouldNotStartGameWithEmptyTeam() {
        Lobby lobby = new Lobby(host, 4);

        assertThrows(InvalidStartLobbyException.class, () -> lobby.startGame(hostId));
    }


    @Test
    void shouldNotStartGameWith4TeamsWithoutLabel() {
        assertThrows(InvalidStartLobbyException.class, () -> lobby4Teams.startGame(hostId));
    }

    @Test
    void shouldNotStartGameWith6TeamsWithoutLabel() {
        assertThrows(InvalidStartLobbyException.class, () -> lobby6Teams.startGame(hostId));
    }

    @Test
    void shouldStartGameWith4Teams() {
        lobby4Teams.assignTeam(team1Id, "INGE");
        lobby4Teams.assignTeam(team2Id, "COOP");
        lobby4Teams.assignTeam(team3Id, "EXPE");
        lobby4Teams.assignTeam(team4Id, "GECO");

        assertTrue(lobby4Teams.startGame(host.id()));
    }

    @Test
    void shouldStartGameWith6Teams() {
        lobby6Teams.assignTeam(team1Id, "INGE");
        lobby6Teams.assignTeam(team2Id, "MECA");
        lobby6Teams.assignTeam(team3Id, "EXPE");
        lobby6Teams.assignTeam(team4Id, "GECO");
        lobby6Teams.assignTeam(team5Id, "MEDI");
        lobby6Teams.assignTeam(team6Id, "COOP");

        assertTrue(lobby6Teams.startGame(host.id()));
    }

    @Test
    void shouldNotStartGameWithWrongHostId() {
        lobby4Teams.assignTeam(team1Id, "INGE");
        lobby4Teams.assignTeam(team2Id, "COOP");
        lobby4Teams.assignTeam(team3Id, "EXPE");
        lobby4Teams.assignTeam(team4Id, "GECO");

        UUID wrongHostId = UUID.randomUUID();
        assertThrows(InvalidStartLobbyException.class, () -> lobby4Teams.startGame(wrongHostId));
    }

    @Test
    void shouldNotStartGameWithWrong4Teams() {
        lobby4Teams.assignTeam(team1Id, "INGE");

        assertThrows(IllegalArgumentException.class, () -> lobby4Teams.assignTeam(team2Id, "MECA"));
    }

    @Test
    void shouldNotCreateLobbyWithInvalidNumberOfTeams() {
        Host h = new Host(UUID.randomUUID());

        assertThrows(IllegalArgumentException.class, () -> new Lobby(h, 3));
        assertThrows(IllegalArgumentException.class, () -> new Lobby(h, 5));
        assertThrows(IllegalArgumentException.class, () -> new Lobby(h, 7));
    }

    @Test
    void shouldNotAssignTeamWithUnknownClientId() {
        UUID wrongClientId = UUID.randomUUID();
        assertThrows(NotFoundException.class, () -> lobby4Teams.assignTeam(wrongClientId, "INGE"));
    }

    @Test
    void shouldNotAssignTeamWithDuplicateLabel() {
        lobby4Teams.assignTeam(team1Id, "INGE");

        assertThrows(IllegalArgumentException.class, () -> lobby4Teams.assignTeam(team2Id, "INGE"));
    }

    @Test
    void shouldNotAssignTeamTwice() {
        lobby4Teams.assignTeam(team1Id, "INGE");

        assertThrows(IllegalArgumentException.class, () -> lobby4Teams.assignTeam(team1Id, "COOP"));
    }
}