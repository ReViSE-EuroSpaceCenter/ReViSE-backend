package be.eurospacecenter.revise.dto.event;

public record ResourceEvent(ResourceEventType type, Object payload) {
}
