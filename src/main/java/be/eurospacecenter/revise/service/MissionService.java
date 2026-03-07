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

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
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

    public void changeTeamMissionState(String lobbyCode, UUID clientId, MissionType missionType) {
        MissionManager manager = getManager(lobbyCode);
        manager.changeTeamMissionState(clientId, missionType);

        String teamLabel = manager.getTeamLabel(clientId);
        TeamProgression teamProgression = manager.getTeamProgression(clientId);

        notifier.notifyTeamProgression(lobbyCode, teamLabel, teamProgression);
    }


    public TeamsProgressionResponse getTeamsProgression(String lobbyCode) {
        MissionManager manager = getManager(lobbyCode);
        return new TeamsProgressionResponse(manager.teamsProgression());
    }

    public TeamFullProgressionResponse getTeamFullProgression(String lobbyCode, UUID clientId) {
        MissionManager manager = getManager(lobbyCode);
        TeamFullProgression fullProgression = manager.getTeamFullProgression(clientId);

        return new TeamFullProgressionResponse(fullProgression);
    }

    public void endMission(String lobbyCode, UUID hostId) {
        MissionManager manager = getManager(lobbyCode);
        manager.endMission(hostId);

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