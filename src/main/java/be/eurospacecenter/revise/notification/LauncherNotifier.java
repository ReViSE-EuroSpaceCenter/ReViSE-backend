package be.eurospacecenter.revise.notification;

public interface LauncherNotifier {
    void notifyResourceUpdated(String lobbyCode, String resourceName);
}
