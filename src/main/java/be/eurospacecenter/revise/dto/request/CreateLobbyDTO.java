package be.eurospacecenter.revise.dto.request;

import be.eurospacecenter.revise.exceptions.ErrorKeys;
import jakarta.validation.constraints.NotNull;

public record CreateLobbyDTO(
        @NotNull
        Integer numberOfTeams
) {
    public CreateLobbyDTO {
        if (numberOfTeams != 4 && numberOfTeams != 6) {
            throw new IllegalArgumentException(ErrorKeys.INVALID_NUMBER_OF_TEAMS);
        }
    }
}