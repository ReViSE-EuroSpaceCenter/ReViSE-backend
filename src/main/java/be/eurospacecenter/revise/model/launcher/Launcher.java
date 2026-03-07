package be.eurospacecenter.revise.model.launcher;

import be.eurospacecenter.revise.exceptions.ErrorKeys;
import be.eurospacecenter.revise.exceptions.NoAutoriseOperationException;
import be.eurospacecenter.revise.model.GameInfo;

import java.util.Map;
import java.util.UUID;

public class Launcher {
    private final GameInfo gameInfo;

    public Launcher(GameInfo gameInfo) {
        this.gameInfo = gameInfo;
    }

    public String getTeamLabel(UUID id) {
        return gameInfo.getTeam(id).getLabel();
    }

    public void updateResources(UUID clientId, Map<ResourceType, Integer> resources) {
        ensureClient(clientId);
        gameInfo.getTeam(clientId).removeResources(resources);
    }

    public int getGeneralScore(UUID hostId) {
        ensureHost(hostId);
        return gameInfo.getTotalScore();
    }

    private void ensureHost(UUID hostId) {
        if (gameInfo.isNotHost(hostId)) {
            throw new NoAutoriseOperationException(ErrorKeys.ACTION_RESERVED_TO_HOST);
        }
    }

    private void ensureClient(UUID clientId) {
        if (gameInfo.isNotClient(clientId)) {
            throw new NoAutoriseOperationException(ErrorKeys.CLIENT_NOT_IN_LOBBY);
        }
    }
}
