package be.eurospacecenter.revise.dto.response;

import be.eurospacecenter.revise.model.TeamProgression;

public record TeamProgressionResponse(String teamLabel, TeamProgression teamProgression) {
}
