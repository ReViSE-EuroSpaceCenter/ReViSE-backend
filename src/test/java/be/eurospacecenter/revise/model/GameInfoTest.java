package be.eurospacecenter.revise.model;

import be.eurospacecenter.revise.exceptions.NotFoundException;
import be.eurospacecenter.revise.model.lobby.Host;
import be.eurospacecenter.revise.model.lobby.Team;
import be.eurospacecenter.revise.model.lobby.TeamLabel;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class GameInfoTest {

    @Test
    void foundTeamByLabel() {
        GameInfo gameInfo = new GameInfo(new Host(UUID.randomUUID()), LocalDateTime.now());
        Team team = new Team(TeamLabel.EXPE, UUID.randomUUID());
        gameInfo.addTeam(team);

        assertEquals(team, gameInfo.getTeamByLabel(TeamLabel.EXPE.toString()));
    }

    @Test
    void foundTeamByLabelFor4Teams() {
        Set<TeamLabel> teams = TeamLabel.getAllowedLabels(true);
        GameInfo gameInfo = new GameInfo(new Host(UUID.randomUUID()), LocalDateTime.now());

        teams.forEach(t -> {
            Team team = new Team(t, UUID.randomUUID());
            gameInfo.addTeam(team);
        });

        teams.forEach(t -> assertDoesNotThrow(() -> gameInfo.getTeamByLabel(t.toString())));
    }

    @Test
    void foundTeamByLabelShouldThrowExceptionIfTeamNotFound() {
        GameInfo gameInfo = new GameInfo(new Host(UUID.randomUUID()), LocalDateTime.now());
        String label = TeamLabel.EXPE.toString();

        assertThrows(NotFoundException.class, () -> gameInfo.getTeamByLabel(label));
    }

}