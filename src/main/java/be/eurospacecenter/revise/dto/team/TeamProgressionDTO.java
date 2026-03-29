package be.eurospacecenter.revise.dto.team;

import be.eurospacecenter.revise.model.lobby.TeamLabel;
import be.eurospacecenter.revise.model.mission.TeamProgression;

public record TeamProgressionDTO(
        TeamLabel teamLabel,
        int classicMissionsCompleted,
        boolean firstBonusMissionCompleted,
        boolean secondBonusMissionCompleted,
        boolean allTeamsMissionsCompleted
) {

    public static TeamProgressionDTO fromTeamProgression(TeamProgression teamProgression) {
        return new TeamProgressionDTO(
                teamProgression.teamLabel(),
                teamProgression.classicMissionsCompleted(),
                teamProgression.firstBonusMissionCompleted(),
                teamProgression.secondBonusMissionCompleted(),
                teamProgression.allTeamsMissionsCompleted()
        );
    }
}
