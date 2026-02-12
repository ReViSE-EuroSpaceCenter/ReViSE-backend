package be.eurospacecenter.revise.service;

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
        games.put(lobbyCode, game);
    }

    public int getGeneralScore(String lobbyCode) {
        Game game = getGame(lobbyCode);
        return game.generalScore();
    }

    public Game getGame(String lobbyCode) {
        return games.get(lobbyCode);
    }
}