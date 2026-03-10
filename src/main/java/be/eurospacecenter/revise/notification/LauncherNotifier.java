package be.eurospacecenter.revise.notification;

import be.eurospacecenter.revise.model.launcher.TeamResources;

public interface LauncherNotifier {
    void notifyResourcesUpdated(String lobbyCode, TeamResources teamResources);
}
