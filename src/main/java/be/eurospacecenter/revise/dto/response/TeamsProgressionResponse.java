package be.eurospacecenter.revise.dto.response;

import be.eurospacecenter.revise.model.lobby.TeamLabel;
import be.eurospacecenter.revise.model.mission.TeamProgression;

import java.util.Map;

public record TeamsProgressionResponse(Map<TeamLabel, TeamProgression> teamsProgression, boolean allTeamsCompleted) {
}
