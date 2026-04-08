package be.eurospacecenter.revise.service;

import be.eurospacecenter.revise.exceptions.ErrorKeys;
import be.eurospacecenter.revise.exceptions.InvalidGameStateException;
import be.eurospacecenter.revise.exceptions.NotFoundException;
import be.eurospacecenter.revise.metric.MetricType;
import be.eurospacecenter.revise.metric.RecordMetric;
import be.eurospacecenter.revise.model.GameInfo;
import be.eurospacecenter.revise.model.GameState;
import be.eurospacecenter.revise.model.discover.DiscoverManager;
import be.eurospacecenter.revise.model.lobbycode.LobbyCode;
import be.eurospacecenter.revise.model.resource.TeamsResources;
import be.eurospacecenter.revise.notification.DiscoverNotifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DiscoverService implements Cleanable, Workflow {

    private static final GameState STATE = GameState.DISCOVER;

    final Map<LobbyCode, DiscoverManager> managers = new ConcurrentHashMap<>();

    private final DiscoverNotifier notifier;

    public DiscoverService(DiscoverNotifier notifier) {
        this.notifier = notifier;
    }

    public TeamsResources getTeamsResources(LobbyCode lobbyCode, UUID hostId) {
        DiscoverManager discoverManager = getManager(lobbyCode);

        return discoverManager.getTeamsResources(hostId);
    }

    @RecordMetric(MetricType.GAME_ENDED)
    public void endGame(LobbyCode lobbyCode, UUID hostId) {
        DiscoverManager discoverManager = getManager(lobbyCode);

        discoverManager.endGame(hostId);

        notifier.notifyGameEnded(lobbyCode);
    }

    @Override
    public void registerManager(LobbyCode lobbyCode, GameInfo gameInfo) {
        if (lobbyCode == null) {
            throw new IllegalArgumentException(ErrorKeys.INVALID_LOBBY_CODE);
        }

        gameInfo.changeState(STATE);

        managers.put(lobbyCode, new DiscoverManager(gameInfo));
    }

    @Override
    public void cleanUp(List<LobbyCode> toRemove) {
        toRemove.forEach(managers::remove);
    }

    private DiscoverManager getManager(LobbyCode lobbyCode) {
        DiscoverManager manager = Optional.ofNullable(managers.get(lobbyCode))
                .orElseThrow(() -> new NotFoundException(ErrorKeys.DISCOVER_MANAGER_NOT_FOUND));

        GameState currentState = manager.getGameInfo().getState();

        if (currentState != STATE) {
            throw new InvalidGameStateException(currentState);
        }

        return manager;
    }
}
