package be.eurospacecenter.revise.model.mission;

import be.eurospacecenter.revise.model.lobby.TeamLabel;

import java.util.Map;

public record TeamsProgression(
        Map<TeamLabel, TeamFullProgression> teamsFullProgression,
        boolean allTeamsMissionsCompleted) {
}
