package be.eurospacecenter.revise.dto.lobby;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record LobbyStartedDTO(
        @NotNull
        UUID hostId
) {}