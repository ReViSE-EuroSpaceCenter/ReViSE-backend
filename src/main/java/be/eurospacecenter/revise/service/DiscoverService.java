package be.eurospacecenter.revise.service;

import be.eurospacecenter.revise.exceptions.ErrorKeys;
import be.eurospacecenter.revise.exceptions.NotFoundException;
import be.eurospacecenter.revise.model.GameInfo;
import be.eurospacecenter.revise.model.discover.DiscoverManager;

import be.eurospacecenter.revise.model.discover.ResourceType;
import be.eurospacecenter.revise.model.discover.TeamResources;
import be.eurospacecenter.revise.model.lobbycode.LobbyCode;
import be.eurospacecenter.revise.notification.DiscoverNotifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DiscoverService implements Cleanable {

    protected final Map<LobbyCode, DiscoverManager> managers = new ConcurrentHashMap<>();
    private final DiscoverNotifier notifier;

    public DiscoverService(DiscoverNotifier notifier) {
        this.notifier = notifier;
    }

    public void registerDiscover(LobbyCode lobbyCode, GameInfo gameInfo) {
        if (lobbyCode == null) {
            throw new IllegalArgumentException(ErrorKeys.INVALID_LOBBY_CODE);
        }
        managers.put(lobbyCode, new DiscoverManager(gameInfo));
    }

    public void updateResources(LobbyCode lobbyCode, UUID clientId, Map<ResourceType, Integer> resources) {
        DiscoverManager discoverManager = getManager(lobbyCode);

        TeamResources teamResources = discoverManager.updateResources(clientId, resources);

        notifier.notifyResourcesUpdated(lobbyCode, teamResources);
    }

    public int getTeamsScore(LobbyCode lobbyCode, UUID hostId) {
        DiscoverManager discoverManager = getManager(lobbyCode);

        return discoverManager.getTeamsScore(hostId);
    }

    private DiscoverManager getManager(LobbyCode lobbyCode) {
        return Optional.ofNullable(managers.get(lobbyCode)).orElseThrow(() -> new NotFoundException(ErrorKeys.DISCOVER_MANAGER_NOT_FOUND));
    }

    @Override
    public void cleanUp(List<LobbyCode> toRemove) {
        toRemove.forEach(managers::remove);
    }
}
