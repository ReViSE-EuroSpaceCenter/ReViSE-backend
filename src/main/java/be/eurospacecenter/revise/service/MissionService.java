package be.eurospacecenter.revise.service;

import be.eurospacecenter.revise.dto.response.TeamFullProgressionDTO;
import be.eurospacecenter.revise.dto.response.TeamsProgressionDTO;
import be.eurospacecenter.revise.exceptions.ErrorKeys;
import be.eurospacecenter.revise.exceptions.NotFoundException;
import be.eurospacecenter.revise.model.*;
import be.eurospacecenter.revise.model.lobby.TeamLabel;
import be.eurospacecenter.revise.model.lobbycode.LobbyCode;
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

    protected final Map<LobbyCode, MissionManager> managers = new ConcurrentHashMap<>();
    private final MissionNotifier notifier;
    private final LauncherService launcherService;

    public MissionService(MissionNotifier notifier, LauncherService launcherService) {
        this.notifier = notifier;
        this.launcherService = launcherService;
    }

    public void registerManager(LobbyCode lobbyCode, GameInfo gameInfo) {
        if (lobbyCode == null) {
            throw new IllegalArgumentException(ErrorKeys.INVALID_LOBBY_CODE);
        }

        managers.put(lobbyCode, new MissionManager(gameInfo));
    }

    public void changeTeamMissionsState(LobbyCode lobbyCode, UUID id, TeamLabel teamLabel, Set<MissionType> missionType) {
        MissionManager manager = getManager(lobbyCode);

        TeamProgression teamProgression = manager.changeTeamMissionsState(id, teamLabel, missionType);

        notifier.notifyTeamProgression(lobbyCode.lobbyCode(), teamProgression);
    }


    public TeamsProgressionDTO getTeamsFullProgression(LobbyCode lobbyCode) {
        MissionManager manager = getManager(lobbyCode);

        return TeamsProgressionDTO.fromTeamsProgression(manager.getTeamsFullProgression());
    }

    public TeamFullProgressionDTO getTeamFullProgression(LobbyCode lobbyCode, UUID clientId) {
        MissionManager manager = getManager(lobbyCode);
        TeamFullProgression fullProgression = manager.getTeamFullProgression(clientId);

        return TeamFullProgressionDTO.fromTeamFullProgression(fullProgression);
    }

    public void endMission(LobbyCode lobbyCode, UUID hostId) {
        MissionManager manager = getManager(lobbyCode);
        manager.validateEndOfMission(hostId);

        launcherService.registerLauncher(lobbyCode, manager.getGameInfo());

        notifier.notifyMissionEnded(lobbyCode.lobbyCode());
    }

    public MissionManager getManager(LobbyCode lobbyCode) {
        return Optional.ofNullable(managers.get(lobbyCode)).orElseThrow(() -> new NotFoundException(ErrorKeys.MISSION_MANAGER_NOT_FOUND));
    }

    @Override
    public void cleanUp(List<LobbyCode> toRemove) {
        toRemove.forEach(managers::remove);
    }
}