package be.eurospacecenter.revise.notification;

public interface LobbyNotifier {
    void notifyTeamJoined(String lobbyCode, String teamLabel);
    void notifyGameStarted(String lobbyCode);
}
