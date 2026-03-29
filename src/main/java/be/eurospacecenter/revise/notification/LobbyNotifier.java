package be.eurospacecenter.revise.notification;

import be.eurospacecenter.revise.model.lobby.TeamLabel;
import be.eurospacecenter.revise.model.lobbycode.LobbyCode;

public interface LobbyNotifier {
    void notifyTeamJoined(LobbyCode lobbyCode, TeamLabel teamLabel);
    void notifyClientJoined(LobbyCode lobbyCode);
    void notifyGameStarted(LobbyCode lobbyCode);
}
