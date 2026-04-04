package be.eurospacecenter.revise.model.resource;

import be.eurospacecenter.revise.exceptions.ErrorKeys;
import be.eurospacecenter.revise.exceptions.NoAutoriseOperationException;
import be.eurospacecenter.revise.model.GameInfo;

import java.util.Map;
import java.util.UUID;

public class ResourceManager {
    private final GameInfo gameInfo;

    public ResourceManager(GameInfo gameInfo) {
        this.gameInfo = gameInfo;
    }

    public GameInfo getGameInfo() {
        return gameInfo;
    }

    public void ensureHost(UUID hostId) {
        if (gameInfo.isNotHost(hostId)) {
            throw new NoAutoriseOperationException(ErrorKeys.ACTION_RESERVED_TO_HOST);
        }
    }

    public TeamResources updateResources(UUID clientId, Map<ResourceType, Integer> resources) {
        if (gameInfo.isNotClient(clientId)) {
            throw new NoAutoriseOperationException(ErrorKeys.CLIENT_NOT_IN_LOBBY);
        }
        return gameInfo.getTeam(clientId).updateResources(resources);
    }
}

