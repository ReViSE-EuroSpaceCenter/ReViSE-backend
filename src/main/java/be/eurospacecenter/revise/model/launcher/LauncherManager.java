package be.eurospacecenter.revise.model.launcher;

import be.eurospacecenter.revise.exceptions.ErrorKeys;
import be.eurospacecenter.revise.exceptions.NoAutoriseOperationException;
import be.eurospacecenter.revise.model.GameInfo;
import be.eurospacecenter.revise.model.GameState;
import be.eurospacecenter.revise.model.Team;
import be.eurospacecenter.revise.model.mission.TeamsProgression;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class LauncherManager {

    private final GameInfo gameInfo;

    public LauncherManager(GameInfo gameInfo) {
        this.gameInfo = gameInfo;
    }

    public GameInfo getGameInfo() {
        return gameInfo;
    }

    public TeamsProgression getTeamsFullProgression() {
        Map<UUID, Team> teams = gameInfo.getTeams();

        return new TeamsProgression(
                teams.values().stream().collect(Collectors.toMap(Team::getLabel, Team::getFullProgression)),
                true
        );
    }

    public void endGame(UUID hostId) {
        if (gameInfo.isNotHost(hostId)) {
            throw new NoAutoriseOperationException(ErrorKeys.ACTION_RESERVED_TO_HOST);
        }

        gameInfo.changeState(GameState.END);
    }

    public void ensureHost(UUID hostId) {
        if (gameInfo.isNotHost(hostId)) {
            throw new NoAutoriseOperationException(ErrorKeys.ACTION_RESERVED_TO_HOST);
        }
    }
}
