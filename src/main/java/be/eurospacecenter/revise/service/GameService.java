package be.eurospacecenter.revise.service;

import be.eurospacecenter.revise.exceptions.InvalidGameOperationException;
import be.eurospacecenter.revise.exceptions.InvalidStartLobbyException;
import be.eurospacecenter.revise.model.*;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GameService {

    protected final Map<String, Game> games = new ConcurrentHashMap<>();

    public GameService() {
        // Rajouter maybe notifier
    }

    public void registerGame(String lobbyCode, Game game) {
        if (lobbyCode == null || lobbyCode.isEmpty()) {
            throw new InvalidStartLobbyException("Impossible d'enregistrer une Game avec un lobby code invalide");
        }
        games.put(lobbyCode, game);
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
}