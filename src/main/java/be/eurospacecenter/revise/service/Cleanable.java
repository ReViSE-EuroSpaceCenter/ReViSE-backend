package be.eurospacecenter.revise.service;

import be.eurospacecenter.revise.model.lobbycode.LobbyCode;

import java.util.List;

public interface Cleanable {
    void cleanUp(List<LobbyCode> toRemove);
}
