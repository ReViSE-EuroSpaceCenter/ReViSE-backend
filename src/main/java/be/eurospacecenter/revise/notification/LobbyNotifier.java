package be.eurospacecenter.revise.notification;

public interface LobbyNotifier {
    void notifyTeamJoined(String lobbyCode, String teamLabel);
    void notifyClientJoined(String lobbyCode);
    void notifyGameStarted(String lobbyCode);
}
