package be.eurospacecenter.revise.service;

import be.eurospacecenter.revise.exceptions.ErrorKeys;
import be.eurospacecenter.revise.exceptions.NotFoundException;
import be.eurospacecenter.revise.model.GameInfo;
import be.eurospacecenter.revise.model.launcher.Launcher;

import be.eurospacecenter.revise.model.launcher.ResourceType;
import be.eurospacecenter.revise.notification.LauncherNotifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LauncherService implements Cleanable {

    protected final Map<String, Launcher> launchers = new ConcurrentHashMap<>();
    private final LauncherNotifier notifier;

    public LauncherService(LauncherNotifier notifier) {
        this.notifier = notifier;
    }

    public void registerLauncher(String lobbyCode, GameInfo gameInfo) {
        if (lobbyCode == null || lobbyCode.isEmpty()) {
            throw new IllegalArgumentException(ErrorKeys.INVALID_LOBBY_CODE);
        }
        launchers.put(lobbyCode, new Launcher(gameInfo));
    }

    public void updateResources(String lobbyCode, UUID clientId, Map<ResourceType, Integer> resources) {
        Launcher launcher = getLauncher(lobbyCode);
        launcher.updateResources(clientId, resources);

        String teamLabel = launcher.getTeamLabel(clientId);
        notifier.notifyResourcesUpdated(lobbyCode, teamLabel);
    }

    public int getGeneralScore(String lobbyCode, UUID hostId) {
        Launcher launcher = getLauncher(lobbyCode);
        return launcher.getGeneralScore(hostId);
    }

    private Launcher getLauncher(String lobbyCode) {
        return Optional.ofNullable(launchers.get(lobbyCode)).orElseThrow(() -> new NotFoundException(ErrorKeys.LAUNCHER_NOT_FOUND));
    }

    @Override
    public void cleanUp(List<String> toRemove) {
        toRemove.forEach(launchers::remove);
    }
}
