package be.eurospacecenter.revise.dto.lobby;

import be.eurospacecenter.revise.model.lobby.LobbyManager;
import be.eurospacecenter.revise.model.lobby.TeamLabel;

import java.util.Set;

public record LobbyInfoDTO(Set<TeamLabel> availableTeams, Set<TeamLabel> allTeams) {
    public static LobbyInfoDTO fromLobby(LobbyManager lobbyManager) {
        return new LobbyInfoDTO(lobbyManager.getAvailableTeamLabels(), lobbyManager.getAllTeamLabels());
    }
}
