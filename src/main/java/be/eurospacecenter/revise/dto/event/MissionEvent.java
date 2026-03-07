package be.eurospacecenter.revise.dto.event;

public record MissionEvent(MissionEventType type, Object payload) {
}

