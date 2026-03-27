package be.eurospacecenter.revise.service;

import be.eurospacecenter.revise.exceptions.ErrorKeys;
import be.eurospacecenter.revise.exceptions.NotFoundException;
import be.eurospacecenter.revise.model.GameInfo;
import be.eurospacecenter.revise.model.launcher.LauncherManager;

import be.eurospacecenter.revise.model.launcher.ResourceType;
import be.eurospacecenter.revise.model.launcher.TeamResources;
import be.eurospacecenter.revise.model.lobbycode.LobbyCode;
import be.eurospacecenter.revise.notification.LauncherNotifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LauncherService implements Cleanable {

    protected final Map<LobbyCode, LauncherManager> launchers = new ConcurrentHashMap<>();
    private final LauncherNotifier notifier;

    public LauncherService(LauncherNotifier notifier) {
        this.notifier = notifier;
    }

    public void registerLauncher(LobbyCode lobbyCode, GameInfo gameInfo) {
        if (lobbyCode == null) {
            throw new IllegalArgumentException(ErrorKeys.INVALID_LOBBY_CODE);
        }
        launchers.put(lobbyCode, new LauncherManager(gameInfo));
    }

    public void updateResources(LobbyCode lobbyCode, UUID clientId, Map<ResourceType, Integer> resources) {
        LauncherManager launcherManager = getLauncher(lobbyCode);
        TeamResources teamResources = launcherManager.updateResources(clientId, resources);

        notifier.notifyResourcesUpdated(lobbyCode, teamResources);
    }

    public int getTeamsScore(LobbyCode lobbyCode, UUID hostId) {
        LauncherManager launcherManager = getLauncher(lobbyCode);

        return launcherManager.getTeamsScore(hostId);
    }

    private LauncherManager getLauncher(LobbyCode lobbyCode) {
        return Optional.ofNullable(launchers.get(lobbyCode)).orElseThrow(() -> new NotFoundException(ErrorKeys.LAUNCHER_NOT_FOUND));
    }

    @Override
    public void cleanUp(List<LobbyCode> toRemove) {
        toRemove.forEach(launchers::remove);
    }
}
