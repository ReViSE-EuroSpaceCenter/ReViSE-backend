package be.eurospacecenter.revise.model;

import be.eurospacecenter.revise.exceptions.ErrorKeys;
import be.eurospacecenter.revise.exceptions.NoAutoriseOperationException;
import be.eurospacecenter.revise.exceptions.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class LobbyTest {

    private final Host host = new Host(UUID.randomUUID());
    private final UUID hostId = host.id();

    private final UUID team1Id = UUID.randomUUID();
    private final UUID team2Id = UUID.randomUUID();
    private final UUID team3Id = UUID.randomUUID();
    private final UUID team4Id = UUID.randomUUID();
    private final UUID team5Id = UUID.randomUUID();
    private final UUID team6Id = UUID.randomUUID();

    private final Lobby lobby4Teams = new Lobby(host, 4, LocalDateTime.now());
    private final Lobby lobby6Teams = new Lobby(host, 6, LocalDateTime.now());

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
        Lobby lobby = new Lobby(host, 4, LocalDateTime.now());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () ->  lobby.startGame(hostId)
        );
        assertEquals(ErrorKeys.INVALID_TEAM_LABELS, ex.getMessage());
    }


    @Test
    void shouldNotStartGameWith4TeamsWithoutLabel() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () ->  lobby4Teams.startGame(hostId)
        );
        assertEquals(ErrorKeys.INVALID_TEAM_LABELS, ex.getMessage());
    }

    @Test
    void shouldNotStartGameWith6TeamsWithoutLabel() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () ->  lobby6Teams.startGame(hostId)
        );
        assertEquals(ErrorKeys.INVALID_TEAM_LABELS, ex.getMessage());
    }

    @Test
    void shouldStartGameWith4Teams() {
        lobby4Teams.assignTeam(team1Id, "AERO");
        lobby4Teams.assignTeam(team2Id, "MECA");
        lobby4Teams.assignTeam(team3Id, "EXPE");
        lobby4Teams.assignTeam(team4Id, "GECO");

        assertTrue(lobby4Teams.startGame(host.id()));
    }

    @Test
    void shouldStartGameWith6Teams() {
        lobby6Teams.assignTeam(team1Id, "AERO");
        lobby6Teams.assignTeam(team2Id, "MECA");
        lobby6Teams.assignTeam(team3Id, "EXPE");
        lobby6Teams.assignTeam(team4Id, "GECO");
        lobby6Teams.assignTeam(team5Id, "MEDI");
        lobby6Teams.assignTeam(team6Id, "COOP");

        assertTrue(lobby6Teams.startGame(host.id()));
    }

    @Test
    void shouldNotStartGameWithWrongHostId() {
        lobby4Teams.assignTeam(team1Id, "AERO");
        lobby4Teams.assignTeam(team2Id, "MECA");
        lobby4Teams.assignTeam(team3Id, "EXPE");
        lobby4Teams.assignTeam(team4Id, "GECO");

        UUID wrongHostId = UUID.randomUUID();

        NoAutoriseOperationException ex = assertThrows(
                NoAutoriseOperationException.class,
                () -> lobby4Teams.startGame(wrongHostId)
        );
        assertEquals(ErrorKeys.ACTION_RESERVED_TO_HOST, ex.getMessage());
    }

    @Test
    void shouldNotStartGameWithWrong4Teams() {
        lobby4Teams.assignTeam(team1Id, "AERO");

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () ->  lobby6Teams.startGame(hostId)
        );
        assertEquals(ErrorKeys.INVALID_TEAM_LABELS, ex.getMessage());
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 5, 7, 10})
    void shouldNotCreateLobbyWithInvalidNumberOfTeams(int numberOfTeams) {
        Host h = new Host(UUID.randomUUID());
        LocalDateTime now = LocalDateTime.now();

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new Lobby(h, numberOfTeams, now)
        );

        assertEquals(ErrorKeys.INVALID_NUMBER_OF_TEAMS, ex.getMessage());
    }

    @Test
    void shouldNotAssignTeamWithUnknownClientId() {
        UUID wrongClientId = UUID.randomUUID();

        NotFoundException ex = assertThrows(
                NotFoundException.class,
                () -> lobby4Teams.assignTeam(wrongClientId, "AERO")
        );

        assertEquals(ErrorKeys.TEAM_NOT_FOUND, ex.getMessage());
    }

    @Test
    void shouldNotAssignTeamWithDuplicateLabel() {
        lobby4Teams.assignTeam(team1Id, "AERO");

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () ->  lobby4Teams.assignTeam(team2Id, "AERO")
        );

        assertEquals(ErrorKeys.TEAM_LABEL_ALREADY_TAKEN, ex.getMessage());
    }

    @Test
    void shouldNotAssignTeamTwice() {
        lobby4Teams.assignTeam(team1Id, "AERO");

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () ->  lobby4Teams.assignTeam(team1Id, "EXPE")
        );

        assertEquals(ErrorKeys.CLIENT_ALREADY_CHOSE_TEAM, ex.getMessage());
    }
}