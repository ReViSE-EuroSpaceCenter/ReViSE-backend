package be.eurospacecenter.revise.model.lobby;

import java.util.Set;

public record LobbyJoined(String clientId, Set<TeamLabel> availableTeams, Set<TeamLabel> allTeams) {
}
