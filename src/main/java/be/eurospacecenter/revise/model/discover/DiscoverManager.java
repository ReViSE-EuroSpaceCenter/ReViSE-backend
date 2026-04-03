package be.eurospacecenter.revise.model.discover;

import be.eurospacecenter.revise.exceptions.ErrorKeys;
import be.eurospacecenter.revise.exceptions.NoAutoriseOperationException;
import be.eurospacecenter.revise.model.GameInfo;

import java.util.Map;
import java.util.UUID;

public class DiscoverManager {
    private final GameInfo gameInfo;

    public DiscoverManager(GameInfo gameInfo) {
        this.gameInfo = gameInfo;
    }

    public TeamResources updateResources(UUID clientId, Map<ResourceType, Integer> resources) {
        if (gameInfo.isNotClient(clientId)) {
            throw new NoAutoriseOperationException(ErrorKeys.CLIENT_NOT_IN_LOBBY);
        }
        return gameInfo.getTeam(clientId).updateResources(resources);
    }

    public int getTeamsScore(UUID hostId) {
        if (gameInfo.isNotHost(hostId)) {
            throw new NoAutoriseOperationException(ErrorKeys.ACTION_RESERVED_TO_HOST);
        }
        return gameInfo.getTeamsScore();
    }

    public void validateEndOfMission(UUID hostId) {
        if (gameInfo.isNotHost(hostId)) {
            throw new NoAutoriseOperationException(ErrorKeys.ACTION_RESERVED_TO_HOST);
        }
    }
}
