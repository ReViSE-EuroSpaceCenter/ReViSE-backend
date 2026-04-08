package be.eurospacecenter.revise.model;

public enum GameState {
    END(null),
    DISCOVER(END),
    RESOURCE(DISCOVER),
    LAUNCHER(RESOURCE),
    MISSION(LAUNCHER),
    LOBBY(MISSION);

    private final GameState next;

    GameState(GameState next) {
        this.next = next;
    }

    boolean canTransitionTo(GameState newState) {
        return next == newState;
    }
}
