package be.eurospacecenter.revise.notification;

import be.eurospacecenter.revise.model.mission.TeamProgression;

public interface MissionNotifier {
    void notifyTeamProgression(String lobbyCode, TeamProgression teamProgression, boolean allTeamsMissionsCompleted);
    void notifyMissionEnded(String lobbyCode);
}
