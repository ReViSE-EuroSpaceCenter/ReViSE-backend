package be.eurospacecenter.revise.exceptions;

import be.eurospacecenter.revise.model.GameState;

public class InvalidGameStateException extends RuntimeException {
    private final GameState currentState;

    public InvalidGameStateException(GameState currentState) {
        super("Invalid game state: " + currentState);
        this.currentState = currentState;
    }

    public GameState getCurrentState() {
        return currentState;
    }
}
