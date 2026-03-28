package be.eurospacecenter.revise.model.lobby;

import java.util.EnumSet;
import java.util.Set;

public enum TeamLabel {

    AERO, EXPE, GECO, MECA, COOP, MEDI;

    private static final Set<TeamLabel> FOUR_TEAMS_MODE = EnumSet.of(AERO, EXPE, GECO, MECA);

    private static final Set<TeamLabel> SIX_TEAMS_MODE = EnumSet.allOf(TeamLabel.class);

    public static boolean isValidLabel(TeamLabel label, boolean isFourTeamsMode) {
        if (label == null) {
            return false;
        }

        return getAllowedLabels(isFourTeamsMode).contains(label);
    }

    public static boolean isValidTeams(Set<TeamLabel> labels, boolean isFourTeamsMode) {
        Set<TeamLabel> allowed = getAllowedLabels(isFourTeamsMode);

        return labels.size() == allowed.size() && labels.containsAll(allowed);
    }

    public static Set<TeamLabel> getAllowedLabels(boolean isFourTeamsMode) {
        return isFourTeamsMode ? EnumSet.copyOf(FOUR_TEAMS_MODE) :
                EnumSet.copyOf(SIX_TEAMS_MODE);
    }
}
