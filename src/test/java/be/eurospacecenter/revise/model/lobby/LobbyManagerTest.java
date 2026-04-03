package be.eurospacecenter.revise.model.lobby;

import be.eurospacecenter.revise.exceptions.ErrorKeys;
import be.eurospacecenter.revise.exceptions.NoAutoriseOperationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class LobbyManagerTest {

    private final Host host = new Host(UUID.randomUUID());
    private final UUID hostId = host.id();

    private final UUID team1Id = UUID.randomUUID();
    private final UUID team2Id = UUID.randomUUID();
    private final UUID team3Id = UUID.randomUUID();
    private final UUID team4Id = UUID.randomUUID();
    private final UUID team5Id = UUID.randomUUID();
    private final UUID team6Id = UUID.randomUUID();

    private final LobbyManager lobbyManager4Teams = new LobbyManager(host, 4, LocalDateTime.now());
    private final LobbyManager lobbyManager6Teams = new LobbyManager(host, 6, LocalDateTime.now());

    // ---------------------------------------------------------------------
    // Setup
    // ---------------------------------------------------------------------

    @BeforeEach
    void setUp() {
        lobbyManager4Teams.addTeam(team1Id);
        lobbyManager4Teams.addTeam(team2Id);
        lobbyManager4Teams.addTeam(team3Id);
        lobbyManager4Teams.addTeam(team4Id);

        lobbyManager6Teams.addTeam(team1Id);
        lobbyManager6Teams.addTeam(team2Id);
        lobbyManager6Teams.addTeam(team3Id);
        lobbyManager6Teams.addTeam(team4Id);
        lobbyManager6Teams.addTeam(team5Id);
        lobbyManager6Teams.addTeam(team6Id);

    }

    // ---------------------------------------------------------------------
    // Tests
    // ---------------------------------------------------------------------

    @Test
    void shouldNotStartGameWithEmptyTeam() {
        LobbyManager lobbyManager = new LobbyManager(host, 4, LocalDateTime.now());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () ->  lobbyManager.startGame(hostId)
        );
        assertEquals(ErrorKeys.INVALID_TEAM_LABELS, ex.getMessage());
    }


    @Test
    void shouldNotStartGameWith4TeamsWithoutLabel() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () ->  lobbyManager4Teams.startGame(hostId)
        );
        assertEquals(ErrorKeys.INVALID_TEAM_LABELS, ex.getMessage());
    }

    @Test
    void shouldNotStartGameWith6TeamsWithoutLabel() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () ->  lobbyManager6Teams.startGame(hostId)
        );
        assertEquals(ErrorKeys.INVALID_TEAM_LABELS, ex.getMessage());
    }

    @Test
    void shouldStartGameWith4Teams() {
        lobbyManager4Teams.assignTeam(team1Id, TeamLabel.AERO);
        lobbyManager4Teams.assignTeam(team2Id, TeamLabel.MECA);
        lobbyManager4Teams.assignTeam(team3Id, TeamLabel.EXPE);
        lobbyManager4Teams.assignTeam(team4Id, TeamLabel.GECO);

        assertTrue(lobbyManager4Teams.startGame(host.id()));
    }

    @Test
    void shouldStartGameWith6Teams() {
        lobbyManager6Teams.assignTeam(team1Id, TeamLabel.AERO);
        lobbyManager6Teams.assignTeam(team2Id, TeamLabel.MECA);
        lobbyManager6Teams.assignTeam(team3Id, TeamLabel.EXPE);
        lobbyManager6Teams.assignTeam(team4Id, TeamLabel.GECO);
        lobbyManager6Teams.assignTeam(team5Id, TeamLabel.MEDI);
        lobbyManager6Teams.assignTeam(team6Id, TeamLabel.COOP);

        assertTrue(lobbyManager6Teams.startGame(host.id()));
    }

    @Test
    void shouldNotStartGameWithWrongHostId() {
        lobbyManager4Teams.assignTeam(team1Id, TeamLabel.AERO);
        lobbyManager4Teams.assignTeam(team2Id, TeamLabel.MECA);
        lobbyManager4Teams.assignTeam(team3Id, TeamLabel.EXPE);
        lobbyManager4Teams.assignTeam(team4Id, TeamLabel.GECO);

        UUID wrongHostId = UUID.randomUUID();

        NoAutoriseOperationException ex = assertThrows(
                NoAutoriseOperationException.class,
                () -> lobbyManager4Teams.startGame(wrongHostId)
        );
        assertEquals(ErrorKeys.ACTION_RESERVED_TO_HOST, ex.getMessage());
    }

    @Test
    void shouldNotStartGameWithWrong4Teams() {
        lobbyManager4Teams.assignTeam(team1Id, TeamLabel.AERO);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () ->  lobbyManager6Teams.startGame(hostId)
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
                () -> new LobbyManager(h, numberOfTeams, now)
        );

        assertEquals(ErrorKeys.INVALID_NUMBER_OF_TEAMS, ex.getMessage());
    }

    @Test
    void shouldNotAssignTeamWithUnknownClientId() {
        UUID wrongClientId = UUID.randomUUID();

        NoAutoriseOperationException ex = assertThrows(
                NoAutoriseOperationException.class,
                () -> lobbyManager4Teams.assignTeam(wrongClientId, TeamLabel.AERO)
        );

        assertEquals(ErrorKeys.CLIENT_NOT_IN_LOBBY, ex.getMessage());
    }

    @Test
    void shouldNotAssignTeamWithDuplicateLabel() {
        lobbyManager4Teams.assignTeam(team1Id, TeamLabel.AERO);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () ->  lobbyManager4Teams.assignTeam(team2Id, TeamLabel.AERO)
        );

        assertEquals(ErrorKeys.TEAM_LABEL_ALREADY_TAKEN, ex.getMessage());
    }

    @Test
    void shouldNotAssignTeamTwice() {
        lobbyManager4Teams.assignTeam(team1Id, TeamLabel.AERO);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () ->  lobbyManager4Teams.assignTeam(team1Id, TeamLabel.EXPE)
        );

        assertEquals(ErrorKeys.CLIENT_ALREADY_CHOSE_TEAM, ex.getMessage());
    }

    @Test
    void shouldNotAssignNullTeamLabel() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () ->  lobbyManager4Teams.assignTeam(team1Id, null)
        );

        assertEquals(ErrorKeys.INVALID_TEAM_LABEL, ex.getMessage());
    }
}