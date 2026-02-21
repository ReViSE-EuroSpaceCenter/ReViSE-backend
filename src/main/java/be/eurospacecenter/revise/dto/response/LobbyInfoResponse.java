package be.eurospacecenter.revise.dto.response;

import java.util.List;

public record LobbyInfoResponse(List<String> availableTeams, List<String> allTeams) {
}
