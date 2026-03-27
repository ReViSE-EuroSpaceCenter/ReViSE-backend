package be.eurospacecenter.revise.dto.lobby;

import be.eurospacecenter.revise.model.lobby.LobbyCreation;

public record LobbyCreationDTO(String lobbyCode, String hostId) {
    public static LobbyCreationDTO fromLobbyCreation(LobbyCreation lobbyCreation) {
        return new LobbyCreationDTO(lobbyCreation.lobbyCode().lobbyCode(), lobbyCreation.hostId().toString());
    }
}