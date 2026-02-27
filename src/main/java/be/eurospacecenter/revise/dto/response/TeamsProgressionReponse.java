package be.eurospacecenter.revise.dto.response;

import be.eurospacecenter.revise.model.TeamProgression;

import java.util.Map;

public record TeamsProgressionReponse(Map<String, TeamProgression> teamsProgression) {
}
