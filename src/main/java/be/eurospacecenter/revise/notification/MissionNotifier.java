package be.eurospacecenter.revise.notification;

import be.eurospacecenter.revise.model.lobbycode.LobbyCode;
import be.eurospacecenter.revise.model.mission.TeamProgression;

public interface MissionNotifier {
    void notifyTeamProgression(LobbyCode lobbyCode, TeamProgression teamProgression);
    void notifyMissionEnded(LobbyCode lobbyCode);
}
