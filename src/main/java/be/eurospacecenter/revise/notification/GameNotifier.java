package be.eurospacecenter.revise.notification;

import be.eurospacecenter.revise.model.TeamProgression;

public interface GameNotifier {
    void notifyTeamProgression(String lobbyCode, String teamLabel, TeamProgression teamProgression);
}
