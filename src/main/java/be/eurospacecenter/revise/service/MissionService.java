package be.eurospacecenter.revise.service;

import be.eurospacecenter.revise.dto.response.TeamFullProgressionResponse;
import be.eurospacecenter.revise.dto.response.TeamsProgressionResponse;
import be.eurospacecenter.revise.exceptions.ErrorKeys;
import be.eurospacecenter.revise.exceptions.NotFoundException;
import be.eurospacecenter.revise.model.*;
import be.eurospacecenter.revise.model.mission.MissionManager;
import be.eurospacecenter.revise.model.mission.MissionType;
import be.eurospacecenter.revise.model.mission.TeamFullProgression;
import be.eurospacecenter.revise.model.mission.TeamProgression;
import be.eurospacecenter.revise.notification.MissionNotifier;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MissionService implements Cleanable {

    protected final Map<String, MissionManager> managers = new ConcurrentHashMap<>();
    private final MissionNotifier notifier;
    private final LauncherService launcherService;

    public MissionService(MissionNotifier notifier, LauncherService launcherService) {
        this.notifier = notifier;
        this.launcherService = launcherService;
    }

    public void registerManager(String lobbyCode, GameInfo gameInfo) {
        if (lobbyCode == null || lobbyCode.isEmpty()) {
            throw new IllegalArgumentException(ErrorKeys.INVALID_LOBBY_CODE);
        }
        managers.put(lobbyCode, new MissionManager(gameInfo));
    }

    public void changeTeamMissionsState(String lobbyCode, UUID id, String teamLabel, List<MissionType> missionType) {
        MissionManager manager = getManager(lobbyCode);

        TeamProgression teamProgression = manager.changeTeamMissionsState(id, teamLabel, missionType);
        boolean allTeamsMissionsCompleted = manager.isAllTeamsMissionsCompleted();

        notifier.notifyTeamProgression(lobbyCode, teamProgression, allTeamsMissionsCompleted);
    }


    public TeamsProgressionResponse getTeamsProgression(String lobbyCode) {
        MissionManager manager = getManager(lobbyCode);

        return new TeamsProgressionResponse(manager.getTeamsProgression(), manager.isAllTeamsMissionsCompleted());
    }

    public TeamFullProgressionResponse getTeamFullProgression(String lobbyCode, UUID clientId) {
        MissionManager manager = getManager(lobbyCode);
        TeamFullProgression fullProgression = manager.getTeamFullProgression(clientId);

        return new TeamFullProgressionResponse(fullProgression);
    }

    public void endMission(String lobbyCode, UUID hostId) {
        MissionManager manager = getManager(lobbyCode);
        manager.validateEndOfMission(hostId);

        launcherService.registerLauncher(lobbyCode, manager.getGameInfo());

        notifier.notifyMissionEnded(lobbyCode);
    }

    public MissionManager getManager(String lobbyCode) {
        return Optional.ofNullable(managers.get(lobbyCode)).orElseThrow(() -> new NotFoundException(ErrorKeys.MISSION_MANAGER_NOT_FOUND));
    }

    @Override
    public void cleanUp(List<String> toRemove) {
        toRemove.forEach(managers::remove);
    }
}