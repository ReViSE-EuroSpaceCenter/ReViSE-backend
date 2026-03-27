package be.eurospacecenter.revise.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record MissionEndDTO(@NotNull UUID hostId) {
}
