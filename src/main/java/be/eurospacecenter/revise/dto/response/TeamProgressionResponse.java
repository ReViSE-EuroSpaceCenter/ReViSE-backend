package be.eurospacecenter.revise.dto.response;

import be.eurospacecenter.revise.model.mission.TeamProgression;

public record TeamProgressionResponse(String teamLabel, TeamProgression teamProgression) {
}
