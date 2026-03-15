package be.eurospacecenter.revise.model.lobby;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class TeamLabelTest {

    @Test
    void validLabel_fourTeamsMode() {
        assertTrue(TeamLabel.isValidLabel(TeamLabel.MECA, true));
        assertTrue(TeamLabel.isValidLabel(TeamLabel.EXPE, true));
        assertTrue(TeamLabel.isValidLabel(TeamLabel.GECO, true));
        assertTrue(TeamLabel.isValidLabel(TeamLabel.AERO, true));
    }

    @Test
    void invalidLabel_fourTeamsModeInit() {
        assertFalse(TeamLabel.isValidLabel(TeamLabel.COOP, true));
        assertFalse(TeamLabel.isValidLabel(TeamLabel.MEDI, true));
    }

    @Test
    void validLabel_sixTeamsMode() {
        for (TeamLabel label : TeamLabel.values()) {
            assertTrue(TeamLabel.isValidLabel(label, false));
        }
    }

    @Test
    void nullLabel() {
        assertFalse(TeamLabel.isValidLabel(null, true));
        assertFalse(TeamLabel.isValidLabel(null, false));
    }

    @Test
    void validTeams_fourTeamsMode() {
        Set<TeamLabel> labels = Set.of(TeamLabel.MECA, TeamLabel.EXPE, TeamLabel.GECO, TeamLabel.AERO);

        assertTrue(TeamLabel.isValidTeams(labels, true));
    }

    @Test
    void missingLabel_fourTeamsMode() {
        Set<TeamLabel> labels = Set.of(TeamLabel.COOP, TeamLabel.EXPE, TeamLabel.GECO);

        assertFalse(TeamLabel.isValidTeams(labels, true));
    }

    @Test
    void extraLabel_fourTeamsMode() {
        Set<TeamLabel> labels = Set.of(TeamLabel.COOP, TeamLabel.EXPE, TeamLabel.GECO, TeamLabel.AERO, TeamLabel.MECA);

        assertFalse(TeamLabel.isValidTeams(labels, true));
    }

    @Test
    void invalidLabel_fourTeamsMode() {
        Set<TeamLabel> labels = Set.of(TeamLabel.COOP, TeamLabel.EXPE, TeamLabel.GECO, TeamLabel.MECA);

        assertFalse(TeamLabel.isValidTeams(labels, true));
    }

    @Test
    void validTeams_sixTeamsMode() {
        Set<TeamLabel> labels = Set.of(TeamLabel.COOP, TeamLabel.EXPE, TeamLabel.GECO, TeamLabel.AERO, TeamLabel.MECA, TeamLabel.MEDI);

        assertTrue(TeamLabel.isValidTeams(labels, false));
    }

    @Test
    void fourTeamsMode() {
        Set<TeamLabel> allowed = TeamLabel.getAllowedLabels(true);

        assertEquals(4, allowed.size());
        assertTrue(allowed.containsAll(List.of(TeamLabel.MECA, TeamLabel.EXPE, TeamLabel.GECO, TeamLabel.AERO)));
    }

    @Test
    void sixTeamsMode() {
        Set<TeamLabel> allowed = TeamLabel.getAllowedLabels(false);

        assertEquals(6, allowed.size());
        assertTrue(allowed.containsAll(List.of(TeamLabel.values())));
    }

}