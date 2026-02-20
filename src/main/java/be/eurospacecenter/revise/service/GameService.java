package be.eurospacecenter.revise.service;

import be.eurospacecenter.revise.exceptions.InvalidGameOperationException;
import be.eurospacecenter.revise.exceptions.InvalidStartLobbyException;
import be.eurospacecenter.revise.model.*;
import be.eurospacecenter.revise.notification.GameNotifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
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
            throw new InvalidStartLobbyException("Impossible d'enregistrer une Game avec un lobby code invalide");
        }
        games.put(lobbyCode, game);
    }

    public void completeATeamMission(String lobbyCode, UUID clientId, MissionType missionType, Map<ResourceType, Integer> resources) {
        try {
            Game game = getGame(lobbyCode);
            game.completeTeamMission(clientId, missionType, resources);

            String teamLabel = game.getTeamLabel(clientId);
            notifier.notifyTeamMissionCompleted(lobbyCode, teamLabel, missionType);
        } catch (NullPointerException e) {
            throw new InvalidGameOperationException("Impossible de terminer la mission pour la partie");
        }
    }

    public int getGeneralScore(String lobbyCode) {
        try {
            Game game = getGame(lobbyCode);
            return game.generalScore();
        } catch (NullPointerException e) {
            throw new InvalidGameOperationException("Impossible de récupérer le score de la partie");
        }
    }

    public Game getGame(String lobbyCode) {
        return games.get(lobbyCode);
    }

    protected void clearGames(List<String> toRemove) {
        toRemove.forEach(games::remove);
    }
}