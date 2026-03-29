package be.eurospacecenter.revise.notification;

import be.eurospacecenter.revise.model.launcher.TeamResources;
import be.eurospacecenter.revise.model.lobbycode.LobbyCode;

public interface LauncherNotifier {
    void notifyResourcesUpdated(LobbyCode lobbyCode, TeamResources teamResources);
}
