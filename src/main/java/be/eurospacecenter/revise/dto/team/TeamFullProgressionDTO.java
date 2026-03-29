package be.eurospacecenter.revise.dto.team;

import be.eurospacecenter.revise.model.mission.TeamFullProgression;

import java.util.Map;

public record TeamFullProgressionDTO(
        Map<String, Boolean> completedMissions,
        TeamProgressionDTO teamProgressionDTO
) {
    public static TeamFullProgressionDTO fromTeamFullProgression(TeamFullProgression teamFullProgression) {
        TeamProgressionDTO teamProgressionDTO = TeamProgressionDTO.fromTeamProgression(teamFullProgression.teamProgression());

        return new TeamFullProgressionDTO(
                teamFullProgression.completedMissions(),
                teamProgressionDTO
        );
    }
}
