package be.eurospacecenter.revise.dto.response;

import java.util.List;

public record LobbyJoinedResponse(String clientId, List<String> availableTeams) {
}
