package be.eurospacecenter.revise.model;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public enum TeamLabel {

    COOP, EXPE, GECO, INGE, MECA, MEDI;

    private static final Set<TeamLabel> FOUR_TEAMS_MODE = EnumSet.of(COOP, EXPE, GECO, INGE);

    private static final Set<TeamLabel> SIX_TEAMS_MODE = EnumSet.allOf(TeamLabel.class);

    public static boolean isValidLabel(String label, boolean isFourTeamsMode) {
        if (label == null || label.isBlank()) {
            return false;
        }

        return from(label).map(teamLabel -> getAllowedLabels(isFourTeamsMode).contains(teamLabel)).orElse(false);
    }

    public static boolean isValidTeams(List<String> labels, boolean isFourTeamsMode) {
        Set<TeamLabel> allowed = getAllowedLabels(isFourTeamsMode);

        return labels.size() == allowed.size() && allowed.stream().allMatch(label -> labels.contains(label.name()));
    }

    static Set<TeamLabel> getAllowedLabels(boolean isFourTeamsMode) {
        return isFourTeamsMode ? FOUR_TEAMS_MODE : SIX_TEAMS_MODE;
    }

    private static Optional<TeamLabel> from(String label) {
        try {
            return Optional.of(TeamLabel.valueOf(label));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
