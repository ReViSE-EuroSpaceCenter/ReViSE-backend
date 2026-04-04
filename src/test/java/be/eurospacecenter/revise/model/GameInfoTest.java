package be.eurospacecenter.revise.model;

import be.eurospacecenter.revise.exceptions.ErrorKeys;
import be.eurospacecenter.revise.exceptions.NotFoundException;
import be.eurospacecenter.revise.model.lobby.Host;
import be.eurospacecenter.revise.model.lobby.TeamLabel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class GameInfoTest {

    // ---------------------------------------------------------------------
    // Tests
    // ---------------------------------------------------------------------

    @Test
    void foundTeamByLabel() {
        GameInfo gameInfo = new GameInfo(new Host(UUID.randomUUID()), LocalDateTime.now());
        Team team = new Team(TeamLabel.EXPE, UUID.randomUUID());
        gameInfo.addTeam(team);

        assertEquals(team, gameInfo.getTeamByLabel(TeamLabel.EXPE));
    }

    @Test
    void foundTeamByLabelFor4Teams() {
        Set<TeamLabel> teams = TeamLabel.getAllowedLabels(true);
        GameInfo gameInfo = new GameInfo(new Host(UUID.randomUUID()), LocalDateTime.now());

        teams.forEach(t -> {
            Team team = new Team(t, UUID.randomUUID());
            gameInfo.addTeam(team);
        });

        teams.forEach(t -> assertDoesNotThrow(() -> gameInfo.getTeamByLabel(t)));
    }

    @Test
    void foundTeamByLabelShouldThrowExceptionIfTeamNotFound() {
        GameInfo gameInfo = new GameInfo(new Host(UUID.randomUUID()), LocalDateTime.now());
        assertThrows(NotFoundException.class, () -> gameInfo.getTeamByLabel(TeamLabel.EXPE));
    }

    @ParameterizedTest(name = "Valid transition: {0} → {1}")
    @MethodSource("validTransitions")
    void shouldAllowValidTransitions(GameState from, GameState to) {
        GameInfo gameInfo = new GameInfo(new Host(UUID.randomUUID()), LocalDateTime.now());

        gameInfo.state = from;

        gameInfo.changeState(to);

        assertEquals(to, gameInfo.state);
    }

    static Stream<Arguments> validTransitions() {
        return Stream.of(
                Arguments.of(GameState.LOBBY, GameState.MISSION),
                Arguments.of(GameState.MISSION, GameState.RESOURCE),
                Arguments.of(GameState.RESOURCE, GameState.DISCOVER),
                Arguments.of(GameState.DISCOVER, GameState.END)
        );
    }

    @ParameterizedTest(name = "Invalid transition: {0} → {1}")
    @MethodSource("invalidTransitions")
    void shouldRejectInvalidTransitions(GameState from, GameState to) {
        GameInfo gameInfo = new GameInfo(new Host(UUID.randomUUID()), LocalDateTime.now());

        gameInfo.state = from;

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> gameInfo.changeState(to)
        );

        assertEquals(ErrorKeys.INVALID_GAME_STATE_TRANSITION, exception.getMessage());
        assertEquals(from, gameInfo.state);
    }

    static Stream<Arguments> invalidTransitions() {
        return Stream.of(
                Arguments.of(GameState.LOBBY, GameState.RESOURCE),
                Arguments.of(GameState.LOBBY, GameState.DISCOVER),
                Arguments.of(GameState.LOBBY, GameState.END),
                Arguments.of(GameState.MISSION, GameState.LOBBY),
                Arguments.of(GameState.MISSION, GameState.DISCOVER),
                Arguments.of(GameState.MISSION, GameState.END),
                Arguments.of(GameState.DISCOVER, GameState.LOBBY),
                Arguments.of(GameState.DISCOVER, GameState.MISSION),
                Arguments.of(GameState.DISCOVER, GameState.RESOURCE),
                Arguments.of(GameState.END, GameState.LOBBY),
                Arguments.of(GameState.END, GameState.MISSION),
                Arguments.of(GameState.END, GameState.DISCOVER),
                Arguments.of(GameState.END, GameState.RESOURCE),
                Arguments.of(GameState.LOBBY, GameState.LOBBY),
                Arguments.of(GameState.MISSION, GameState.MISSION),
                Arguments.of(GameState.RESOURCE, GameState.RESOURCE),
                Arguments.of(GameState.DISCOVER, GameState.DISCOVER),
                Arguments.of(GameState.END, GameState.END)
        );
    }

}