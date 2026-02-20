package be.eurospacecenter.revise.model;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class TeamLabelTest {

    @Test
    void validLabel_fourTeamsMode() {
        assertTrue(TeamLabel.isValidLabel("COOP", true));
        assertTrue(TeamLabel.isValidLabel("EXPE", true));
        assertTrue(TeamLabel.isValidLabel("GECO", true));
        assertTrue(TeamLabel.isValidLabel("AERO", true));
    }

    @Test
    void invalidLabel_fourTeamsModeInit() {
        assertFalse(TeamLabel.isValidLabel("MECA", true));
        assertFalse(TeamLabel.isValidLabel("MEDI", true));
    }

    @Test
    void validLabel_sixTeamsMode() {
        for (TeamLabel label : TeamLabel.values()) {
            assertTrue(TeamLabel.isValidLabel(label.name(), false));
        }
    }

    @Test
    void unknownLabel() {
        assertFalse(TeamLabel.isValidLabel("UNKNOWN", true));
        assertFalse(TeamLabel.isValidLabel("UNKNOWN", false));
    }

    @Test
    void nullLabel() {
        assertFalse(TeamLabel.isValidLabel(null, true));
        assertFalse(TeamLabel.isValidLabel(null, false));
    }

    @Test
    void validTeams_fourTeamsMode() {
        List<String> labels = List.of("COOP", "EXPE", "GECO", "AERO");

        assertTrue(TeamLabel.isValidTeams(labels, true));
    }

    @Test
    void missingLabel_fourTeamsMode() {
        List<String> labels = List.of("COOP", "EXPE", "GECO");

        assertFalse(TeamLabel.isValidTeams(labels, true));
    }

    @Test
    void extraLabel_fourTeamsMode() {
        List<String> labels = List.of("COOP", "EXPE", "GECO", "AERO", "MECA");

        assertFalse(TeamLabel.isValidTeams(labels, true));
    }

    @Test
    void invalidLabel_fourTeamsMode() {
        List<String> labels = List.of("COOP", "EXPE", "GECO", "MECA");

        assertFalse(TeamLabel.isValidTeams(labels, true));
    }

    @Test
    void validTeams_sixTeamsMode() {
        List<String> labels = List.of("COOP", "EXPE", "GECO", "AERO", "MECA", "MEDI");

        assertTrue(TeamLabel.isValidTeams(labels, false));
    }

    @Test
    void duplicateLabels() {
        List<String> labels = List.of("COOP", "COOP", "GECO", "AERO");

        assertFalse(TeamLabel.isValidTeams(labels, true));
    }

    @Test
    void unknownLabelForTeam() {
        List<String> labels = List.of("COOP", "EXPE", "UNKNOWN", "AERO");

        assertFalse(TeamLabel.isValidTeams(labels, true));
    }

    @Test
    void fourTeamsMode() {
        Set<TeamLabel> allowed = TeamLabel.getAllowedLabels(true);

        assertEquals(4, allowed.size());
        assertTrue(allowed.containsAll(List.of(TeamLabel.COOP, TeamLabel.EXPE, TeamLabel.GECO, TeamLabel.AERO)));
    }

    @Test
    void sixTeamsMode() {
        Set<TeamLabel> allowed = TeamLabel.getAllowedLabels(false);

        assertEquals(6, allowed.size());
        assertTrue(allowed.containsAll(List.of(TeamLabel.values())));
    }

}