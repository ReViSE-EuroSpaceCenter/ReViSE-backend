package be.eurospacecenter.revise.dto.response;

import be.eurospacecenter.revise.model.lobby.TeamLabel;
import be.eurospacecenter.revise.model.mission.TeamsProgression;

import java.util.Map;
import java.util.stream.Collectors;

public record TeamsProgressionDTO(
        Map<TeamLabel, TeamFullProgressionDTO> teamsFullProgression,
        boolean allTeamsCompleted) {

    public static TeamsProgressionDTO fromTeamsProgression(TeamsProgression teamsProgression) {
        return new TeamsProgressionDTO(
                teamsProgression.teamsFullProgression().entrySet().stream()
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                entry -> TeamFullProgressionDTO.fromTeamFullProgression(entry.getValue())
                        )),
                teamsProgression.allTeamsMissionsCompleted()
        );
    }
}
