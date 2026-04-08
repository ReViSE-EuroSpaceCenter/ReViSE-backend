package be.eurospacecenter.revise.dto.discover;

import be.eurospacecenter.revise.model.resource.ResourceType;
import be.eurospacecenter.revise.model.resource.TeamResources;

import java.util.Map;

public record TeamResourcesDTO(Map<ResourceType, Integer> resources) {

    public static TeamResourcesDTO fromTeamResources(TeamResources teamResources) {
        return new TeamResourcesDTO(teamResources.resources());
    }
}
