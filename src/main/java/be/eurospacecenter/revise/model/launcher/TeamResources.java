package be.eurospacecenter.revise.model.launcher;

import be.eurospacecenter.revise.model.lobby.TeamLabel;

import java.util.Map;

public record TeamResources(
        TeamLabel teamLabel,
        Map<ResourceType, Integer> resources
) {}