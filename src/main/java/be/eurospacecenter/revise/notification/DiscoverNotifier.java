package be.eurospacecenter.revise.notification;

import be.eurospacecenter.revise.model.lobbycode.LobbyCode;

public interface DiscoverNotifier {
    void notifyDiscoverEnded(LobbyCode lobbyCode);
}
