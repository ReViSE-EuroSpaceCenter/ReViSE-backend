package be.eurospacecenter.revise.dto.response;

import be.eurospacecenter.revise.model.mission.TeamProgression;

import java.util.Map;

public record TeamsProgressionResponse(Map<String, TeamProgression> teamsProgression, boolean allTeamsCompleted) {
}
