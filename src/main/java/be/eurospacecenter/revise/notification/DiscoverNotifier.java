package be.eurospacecenter.revise.notification;

import be.eurospacecenter.revise.model.discover.TeamResources;
import be.eurospacecenter.revise.model.lobbycode.LobbyCode;

public interface DiscoverNotifier {
    void notifyResourcesUpdated(LobbyCode lobbyCode, TeamResources teamResources);
}
