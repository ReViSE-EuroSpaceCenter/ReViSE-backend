package be.eurospacecenter.revise.dto.lobby;

import be.eurospacecenter.revise.model.lobby.LobbyJoined;
import be.eurospacecenter.revise.model.lobby.TeamLabel;

import java.util.Set;

public record LobbyJoinedDTO(String clientId, Set<TeamLabel> availableTeams, Set<TeamLabel> allTeams) {
    public static LobbyJoinedDTO fromLobbyJoined(LobbyJoined lobbyJoined) {
        return new LobbyJoinedDTO(lobbyJoined.clientId(), lobbyJoined.availableTeams(), lobbyJoined.allTeams());
    }
}
