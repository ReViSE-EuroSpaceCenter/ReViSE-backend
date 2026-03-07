package be.eurospacecenter.revise.notification;

import be.eurospacecenter.revise.model.mission.TeamProgression;

public interface MissionNotifier {
    void notifyTeamProgression(String lobbyCode, String teamLabel, TeamProgression teamProgression);
    void notifyMissionEnded(String lobbyCode);
}
