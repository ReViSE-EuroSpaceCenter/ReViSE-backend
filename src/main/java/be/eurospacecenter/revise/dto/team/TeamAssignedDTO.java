package be.eurospacecenter.revise.dto.team;

import be.eurospacecenter.revise.model.lobby.TeamLabel;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record TeamAssignedDTO(

        @NotNull
        UUID clientId,

        @NotNull
        TeamLabel teamLabel

) {}