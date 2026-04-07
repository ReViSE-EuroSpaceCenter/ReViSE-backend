package be.eurospacecenter.revise.service;

import be.eurospacecenter.revise.exceptions.ErrorKeys;
import be.eurospacecenter.revise.exceptions.InvalidGameStateException;
import be.eurospacecenter.revise.exceptions.NotFoundException;
import be.eurospacecenter.revise.model.GameInfo;
import be.eurospacecenter.revise.model.GameState;
import be.eurospacecenter.revise.model.resource.ResourceType;
import be.eurospacecenter.revise.model.resource.TeamResources;
import be.eurospacecenter.revise.model.lobbycode.LobbyCode;
import be.eurospacecenter.revise.model.resource.ResourceManager;
import be.eurospacecenter.revise.notification.ResourceNotifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ResourceService implements Cleanable, Workflow {

    private static final GameState STATE = GameState.RESOURCE;

    final Map<LobbyCode, ResourceManager> managers = new ConcurrentHashMap<>();

    private final ResourceNotifier notifier;
    private final DiscoverService discoverService;

    public ResourceService(ResourceNotifier notifier, DiscoverService discoverService) {
        this.notifier = notifier;
        this.discoverService = discoverService;
    }

    public void startResourceEncoding(LobbyCode lobbyCode, UUID hostId) {
        ResourceManager resourceManager = getManager(lobbyCode);

        resourceManager.ensureHost(hostId);

        notifier.notifyEncodingStarted(lobbyCode);
    }

    public void updateResource(LobbyCode lobbyCode, UUID clientId, Map<ResourceType, Integer> resources) {
        ResourceManager resourceManager = getManager(lobbyCode);

        TeamResources teamResources = resourceManager.updateResources(clientId, resources);

        notifier.notifyResourcesUpdated(lobbyCode, teamResources);
    }

    public void endEncodingResources(LobbyCode lobbyCode, UUID hostId) {
        ResourceManager resourceManager = getManager(lobbyCode);

        resourceManager.ensureHost(hostId);
        discoverService.registerManager(lobbyCode, resourceManager.getGameInfo());

        notifier.notifyEncodingEnded(lobbyCode);
    }

    @Override
    public void registerManager(LobbyCode lobbyCode, GameInfo gameInfo) {
        if (lobbyCode == null) {
            throw new IllegalArgumentException(ErrorKeys.INVALID_LOBBY_CODE);
        }

        gameInfo.changeState(STATE);

        managers.put(lobbyCode, new ResourceManager(gameInfo));
    }

    @Override
    public void cleanUp(List<LobbyCode> toRemove) {
        toRemove.forEach(managers::remove);
    }

    private ResourceManager getManager(LobbyCode lobbyCode) {
        ResourceManager manager = Optional.ofNullable(managers.get(lobbyCode))
                .orElseThrow(() -> new NotFoundException(ErrorKeys.RESOURCE_MANAGER_NOT_FOUND));

        GameState currentState = manager.getGameInfo().getState();

        if (currentState != STATE) {
            throw new InvalidGameStateException(currentState);
        }

        return manager;
    }
}
