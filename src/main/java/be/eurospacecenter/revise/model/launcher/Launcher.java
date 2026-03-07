package be.eurospacecenter.revise.model.launcher;

import be.eurospacecenter.revise.exceptions.ErrorKeys;
import be.eurospacecenter.revise.exceptions.NoAutoriseOperationException;
import be.eurospacecenter.revise.model.GameInfo;

import java.util.UUID;

public class Launcher {
    private final GameInfo gameInfo;

    public Launcher(GameInfo gameInfo) {
        this.gameInfo = gameInfo;
    }

    public void updateResources(UUID clientId, String resourceName) {
        ensureClient(clientId);

        throw new UnsupportedOperationException("Resource updates are not implemented yet." + resourceName);
    }

    private void ensureClient(UUID clientId) {
        if (gameInfo.isNotClient(clientId)) {
            throw new NoAutoriseOperationException(ErrorKeys.CLIENT_NOT_IN_LOBBY);
        }
    }
}
