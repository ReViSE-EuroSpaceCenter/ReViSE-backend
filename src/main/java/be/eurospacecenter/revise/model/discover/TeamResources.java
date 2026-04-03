package be.eurospacecenter.revise.model.discover;

import be.eurospacecenter.revise.model.lobby.TeamLabel;

import java.util.Map;

public record TeamResources(
        TeamLabel teamLabel,
        Map<ResourceType, Integer> resources
) {}