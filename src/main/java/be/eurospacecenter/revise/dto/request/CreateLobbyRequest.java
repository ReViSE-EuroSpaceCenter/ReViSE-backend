package be.eurospacecenter.revise.dto.request;

import jakarta.validation.constraints.NotNull;

public record CreateLobbyRequest(
        @NotNull
        Integer numberOfTeams
) {
    public CreateLobbyRequest {
        if (numberOfTeams != 4 && numberOfTeams != 6) {
            throw new IllegalArgumentException("Le nombre d'équipes doit être 4 ou 6");
        }
    }
}