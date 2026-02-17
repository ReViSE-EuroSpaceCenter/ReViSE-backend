package be.eurospacecenter.revise.notification;

import be.eurospacecenter.revise.model.MissionType;

public interface GameNotifier {
    void notifyTeamMissionCompleted(String lobbyCode, String teamLabel, MissionType missionType);
}
