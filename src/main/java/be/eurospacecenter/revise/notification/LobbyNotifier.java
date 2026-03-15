package be.eurospacecenter.revise.notification;

import be.eurospacecenter.revise.model.lobby.TeamLabel;

public interface LobbyNotifier {
    void notifyTeamJoined(String lobbyCode, TeamLabel teamLabel);
    void notifyClientJoined(String lobbyCode);
    void notifyGameStarted(String lobbyCode);
}
