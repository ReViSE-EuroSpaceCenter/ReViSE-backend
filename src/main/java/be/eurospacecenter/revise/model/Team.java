package be.eurospacecenter.revise.model;

import java.util.UUID;

public class Team {
    private final TeamId label;
    private final UUID id;

    public Team(TeamId label, UUID id) {
        this.label = label;
        this.id = id;
    }

    public String label() {
        return label.label;
    }

    public UUID getId() {
        return id;
    }
}