package be.eurospacecenter.revise.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record StartLobbyRequest(
        @NotNull
        UUID hostId
) {}