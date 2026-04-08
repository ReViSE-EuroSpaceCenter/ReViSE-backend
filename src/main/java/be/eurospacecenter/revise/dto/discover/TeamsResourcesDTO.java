package be.eurospacecenter.revise.dto.discover;

import be.eurospacecenter.revise.model.lobby.TeamLabel;
import be.eurospacecenter.revise.model.resource.TeamsResources;

import java.util.Map;
import java.util.stream.Collectors;

public record TeamsResourcesDTO(Map<TeamLabel, TeamResourcesDTO> teamsResources, int totalScore) {

    public static TeamsResourcesDTO fromTeamsResources(TeamsResources teamsResources) {
        return new TeamsResourcesDTO(
                teamsResources.teamsResources().entrySet().stream()
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                entry -> TeamResourcesDTO.fromTeamResources(entry.getValue())
                        )),
                teamsResources.totalScore()
        );
    }
}
