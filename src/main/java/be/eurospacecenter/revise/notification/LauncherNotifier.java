package be.eurospacecenter.revise.notification;

import be.eurospacecenter.revise.model.lobbycode.LobbyCode;

public interface LauncherNotifier {
    void notifyLauncherEnded(LobbyCode lobbyCode);
    void notifyGameEnded(LobbyCode lobbyCode);
}
