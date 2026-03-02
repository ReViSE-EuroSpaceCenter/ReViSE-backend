package be.eurospacecenter.revise.service;

import be.eurospacecenter.revise.dto.response.TeamMissionsResponse;
import be.eurospacecenter.revise.dto.response.TeamsProgressionResponse;
import be.eurospacecenter.revise.exceptions.ErrorKeys;
import be.eurospacecenter.revise.exceptions.NotFoundException;
import be.eurospacecenter.revise.model.*;
import be.eurospacecenter.revise.notification.GameNotifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GameService {

    protected final Map<String, Game> games = new ConcurrentHashMap<>();
    private final GameNotifier notifier;

    public GameService(GameNotifier notifier) {
        this.notifier = notifier;
    }

    public void registerGame(String lobbyCode, Game game) {
        if (lobbyCode == null || lobbyCode.isEmpty()) {
            throw new IllegalArgumentException(ErrorKeys.INVALID_LOBBY_CODE);
        }
        games.put(lobbyCode, game);
    }

    public void changeTeamMissionState(String lobbyCode, UUID clientId, MissionType missionType) {
        Game game = getGame(lobbyCode);
        game.changeTeamMissionState(clientId, missionType);

        String teamLabel = game.getTeamLabel(clientId);
        TeamProgression teamProgression = game.getTeamProgression(clientId);
        notifier.notifyTeamProgression(lobbyCode, teamLabel, teamProgression);
    }


    public TeamsProgressionResponse getTeamsProgression(String lobbyCode) {
        Game game = getGame(lobbyCode);
        return new TeamsProgressionResponse(game.teamsProgression());
    }

    public TeamMissionsResponse getTeamFullProgression(String lobbyCode, UUID clientId) {
        Game game = getGame(lobbyCode);
        TeamFullProgression fullProgression = game.getTeamFullProgression(clientId);

        return new TeamMissionsResponse(fullProgression.completedMissions(), fullProgression.teamProgression());
    }

    public Game getGame(String lobbyCode) {
        return Optional.ofNullable(games.get(lobbyCode)).orElseThrow(() -> new NotFoundException(ErrorKeys.GAME_NOT_FOUND));
    }

    protected void clearGames(List<String> toRemove) {
        toRemove.forEach(games::remove);
    }
}