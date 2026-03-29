package be.eurospacecenter.revise.model.lobby;

import be.eurospacecenter.revise.model.lobbycode.LobbyCode;

import java.util.UUID;

public record LobbyCreation(LobbyCode lobbyCode, UUID hostId) {
}
