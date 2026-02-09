package be.eurospacecenter.revise.notification;

public interface LobbyNotifier {
    void notifyPlayerJoined(String lobbyCode, String teamLabel);
    void notifyGameStarted(String lobbyCode);
}
