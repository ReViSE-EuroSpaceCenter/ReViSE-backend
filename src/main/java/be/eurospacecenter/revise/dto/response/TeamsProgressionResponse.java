package be.eurospacecenter.revise.dto.response;

import be.eurospacecenter.revise.model.mission.TeamFullProgression;

import java.util.Map;

public record TeamsProgressionResponse(Map<String, TeamFullProgression> teamsFullProgression, boolean allTeamsCompleted) {
}
