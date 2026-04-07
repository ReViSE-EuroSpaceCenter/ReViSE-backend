package be.eurospacecenter.revise.service;

import be.eurospacecenter.revise.model.GameInfo;
import be.eurospacecenter.revise.model.lobbycode.LobbyCode;

public interface Workflow {
    void registerManager(LobbyCode lobbyCode, GameInfo gameInfo);
}
