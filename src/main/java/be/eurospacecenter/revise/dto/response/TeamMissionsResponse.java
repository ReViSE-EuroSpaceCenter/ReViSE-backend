package be.eurospacecenter.revise.dto.response;

import be.eurospacecenter.revise.model.TeamProgression;

import java.util.Map;

public record TeamMissionsResponse (Map<String, Boolean> completedMissions, TeamProgression teamProgression) {
}
