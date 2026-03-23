package be.eurospacecenter.revise.dto.response;

import be.eurospacecenter.revise.model.lobby.TeamLabel;

import java.util.Set;

public record LobbyJoinedDTO(String clientId, Set<TeamLabel> availableTeams, Set<TeamLabel> allTeams) {
}
