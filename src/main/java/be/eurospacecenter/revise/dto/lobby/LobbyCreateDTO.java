package be.eurospacecenter.revise.dto.lobby;

import be.eurospacecenter.revise.exceptions.ErrorKeys;
import jakarta.validation.constraints.NotNull;

public record LobbyCreateDTO(
        @NotNull
        Integer numberOfTeams
) {
    public LobbyCreateDTO {
        if (numberOfTeams != 4 && numberOfTeams != 6) {
            throw new IllegalArgumentException(ErrorKeys.INVALID_NUMBER_OF_TEAMS);
        }
    }
}