package be.eurospacecenter.revise.service;

import be.eurospacecenter.revise.exceptions.ErrorKeys;
import be.eurospacecenter.revise.exceptions.InvalidGameStateException;
import be.eurospacecenter.revise.exceptions.NotFoundException;
import be.eurospacecenter.revise.metric.MetricType;
import be.eurospacecenter.revise.metric.RecordMetric;
import be.eurospacecenter.revise.model.GameInfo;
import be.eurospacecenter.revise.model.GameState;
import be.eurospacecenter.revise.model.launcher.LauncherManager;
import be.eurospacecenter.revise.model.lobbycode.LobbyCode;
import be.eurospacecenter.revise.model.mission.TeamsProgression;
import be.eurospacecenter.revise.notification.LauncherNotifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LauncherService implements Cleanable, Workflow {

    private static final GameState STATE = GameState.LAUNCHER;

    final Map<LobbyCode, LauncherManager> managers = new ConcurrentHashMap<>();

    private final LauncherNotifier notifier;
    private final ResourceService resourceService;

    public LauncherService(LauncherNotifier notifier, ResourceService resourceService) {
        this.notifier = notifier;
        this.resourceService = resourceService;
    }


    public TeamsProgression getTeamsProgression(LobbyCode lobbyCode) {
        LauncherManager manager = getManager(lobbyCode);

        return manager.getTeamsFullProgression();
    }

    public void endLauncher(LobbyCode lobbyCode, UUID hostId) {
        LauncherManager launcherManager = getManager(lobbyCode);

        launcherManager.ensureHost(hostId);
        resourceService.registerManager(lobbyCode, launcherManager.getGameInfo());

        notifier.notifyLauncherEnded(lobbyCode);

    }

    @RecordMetric(MetricType.GAME_ENDED)
    public void endGame(LobbyCode lobbyCode, UUID hostId) {
        LauncherManager launcherManager = getManager(lobbyCode);

        launcherManager.endGame(hostId);

        notifier.notifyGameEnded(lobbyCode);
    }

    @Override
    public void cleanUp(List<LobbyCode> toRemove) {
        toRemove.forEach(managers::remove);
    }

    @Override
    public void registerManager(LobbyCode lobbyCode, GameInfo gameInfo) {
        if (lobbyCode == null) {
            throw new IllegalArgumentException(ErrorKeys.INVALID_LOBBY_CODE);
        }

        gameInfo.changeState(STATE);

        managers.put(lobbyCode, new LauncherManager(gameInfo));
    }

    private LauncherManager getManager(LobbyCode lobbyCode) {
        LauncherManager manager = Optional.ofNullable(managers.get(lobbyCode))
                .orElseThrow(() -> new NotFoundException(ErrorKeys.DISCOVER_MANAGER_NOT_FOUND));

        GameState currentState = manager.getGameInfo().getState();

        if (currentState != STATE) {
            throw new InvalidGameStateException(currentState);
        }

        return manager;
    }
}
