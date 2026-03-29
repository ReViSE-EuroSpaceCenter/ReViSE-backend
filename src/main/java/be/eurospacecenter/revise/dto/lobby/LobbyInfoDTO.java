package be.eurospacecenter.revise.dto.lobby;

import be.eurospacecenter.revise.model.lobby.Lobby;
import be.eurospacecenter.revise.model.lobby.TeamLabel;

import java.util.Set;

public record LobbyInfoDTO(Set<TeamLabel> availableTeams, Set<TeamLabel> allTeams) {
    public static LobbyInfoDTO fromLobby(Lobby lobby) {
        return new LobbyInfoDTO(lobby.getAvailableTeamLabels(), lobby.getAllTeamLabels());
    }
}
