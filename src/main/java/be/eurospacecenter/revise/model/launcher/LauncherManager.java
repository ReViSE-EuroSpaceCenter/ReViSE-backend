package be.eurospacecenter.revise.model.launcher;

import be.eurospacecenter.revise.exceptions.ErrorKeys;
import be.eurospacecenter.revise.exceptions.NoAutoriseOperationException;
import be.eurospacecenter.revise.model.GameInfo;

import java.util.Map;
import java.util.UUID;

public class LauncherManager {
    private final GameInfo gameInfo;

    public LauncherManager(GameInfo gameInfo) {
        this.gameInfo = gameInfo;
    }

    public TeamResources updateResources(UUID clientId, Map<ResourceType, Integer> resources) {
        if (gameInfo.isNotClient(clientId)) {
            throw new NoAutoriseOperationException(ErrorKeys.CLIENT_NOT_IN_LOBBY);
        }
        return gameInfo.getTeam(clientId).updateResources(resources);
    }

    public int getGeneralScore(UUID hostId) {
        if (gameInfo.isNotHost(hostId)) {
            throw new NoAutoriseOperationException(ErrorKeys.ACTION_RESERVED_TO_HOST);
        }
        return gameInfo.getTotalScore();
    }
}
