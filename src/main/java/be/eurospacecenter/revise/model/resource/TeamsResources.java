package be.eurospacecenter.revise.model.resource;

import be.eurospacecenter.revise.model.lobby.TeamLabel;

import java.util.Map;

public record TeamsResources(Map<TeamLabel, TeamResources> teamsResources, int totalScore) {
}
