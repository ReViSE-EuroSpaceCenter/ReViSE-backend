package be.eurospacecenter.revise.service;

import be.eurospacecenter.revise.exceptions.ErrorKeys;
import be.eurospacecenter.revise.exceptions.InvalidGameStateException;
import be.eurospacecenter.revise.exceptions.NotFoundException;
import be.eurospacecenter.revise.model.*;
import be.eurospacecenter.revise.model.lobby.TeamLabel;
import be.eurospacecenter.revise.model.lobbycode.LobbyCode;
import be.eurospacecenter.revise.model.mission.*;
import be.eurospacecenter.revise.notification.MissionNotifier;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MissionService implements Cleanable, Workflow {

    private static final GameState STATE = GameState.MISSION;

    final Map<LobbyCode, MissionManager> managers = new ConcurrentHashMap<>();

    private final MissionNotifier notifier;
    private final ResourceService resourceService;

    public MissionService(MissionNotifier notifier, ResourceService resourceService) {
        this.notifier = notifier;
        this.resourceService = resourceService;
    }

    public void changeTeamMissionsState(LobbyCode lobbyCode, UUID id, TeamLabel teamLabel, Set<MissionType> missionType) {
        MissionManager manager = getManager(lobbyCode);

        TeamProgression teamProgression = manager.changeTeamMissionsState(id, teamLabel, missionType);

        notifier.notifyTeamProgression(lobbyCode, teamProgression);
    }


    public TeamsProgression getTeamsProgression(LobbyCode lobbyCode) {
        MissionManager manager = getManager(lobbyCode);

        return manager.getTeamsFullProgression();
    }

    public TeamFullProgression getTeamFullProgression(LobbyCode lobbyCode, UUID clientId) {
        MissionManager manager = getManager(lobbyCode);

        return manager.getTeamFullProgression(clientId);
    }

    public void endMission(LobbyCode lobbyCode, UUID hostId) {
        MissionManager manager = getManager(lobbyCode);

        manager.validateEndOfMission(hostId);
        resourceService.registerManager(lobbyCode, manager.getGameInfo());

        notifier.notifyMissionEnded(lobbyCode);
    }

    @Override
    public void registerManager(LobbyCode lobbyCode, GameInfo gameInfo) {
        if (lobbyCode == null) {
            throw new IllegalArgumentException(ErrorKeys.INVALID_LOBBY_CODE);
        }

        gameInfo.changeState(STATE);

        managers.put(lobbyCode, new MissionManager(gameInfo));
    }

    @Override
    public void cleanUp(List<LobbyCode> toRemove) {
        toRemove.forEach(managers::remove);
    }

    MissionManager getManager(LobbyCode lobbyCode) {
        MissionManager manager = Optional.ofNullable(managers.get(lobbyCode))
                .orElseThrow(() -> new NotFoundException(ErrorKeys.MISSION_MANAGER_NOT_FOUND));

        GameState currentState = manager.getGameInfo().getState();

        if (currentState != STATE) {
            throw new InvalidGameStateException(currentState);
        }

        return manager;
    }
}