package be.eurospacecenter.revise.model;

public enum GameState {
    END(null),
    DISCOVER(END),
    RESOURCE(DISCOVER),
    MISSION(RESOURCE),
    LOBBY(MISSION);

    private final GameState next;

    GameState(GameState next) {
        this.next = next;
    }

    boolean canTransitionTo(GameState newState) {
        return next == newState;
    }
}
