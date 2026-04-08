package be.eurospacecenter.revise.model;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public enum GameState {
    END(),
    DISCOVER(END),
    RESOURCE(DISCOVER),
    LAUNCHER(RESOURCE, END),
    MISSION(LAUNCHER),
    LOBBY(MISSION);

    private final Set<GameState> nextStates;

    GameState(GameState... next) {
        this.nextStates = new HashSet<>(Arrays.asList(next));
    }

    boolean canTransitionTo(GameState newState) {
        return nextStates.contains(newState);
    }
}
