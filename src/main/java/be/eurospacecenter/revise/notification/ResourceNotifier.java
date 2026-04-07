package be.eurospacecenter.revise.notification;

import be.eurospacecenter.revise.model.resource.TeamResources;
import be.eurospacecenter.revise.model.lobbycode.LobbyCode;

public interface ResourceNotifier {
    void notifyEncodingStarted(LobbyCode lobbyCode);
    void notifyResourcesUpdated(LobbyCode lobbyCode, TeamResources teamResources);
    void notifyEncodingEnded(LobbyCode lobbyCode);
}
