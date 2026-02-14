package be.eurospacecenter.revise.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.UUID;

public record AssignTeamRequest(

        @NotNull
        UUID clientId,

        @NotNull
        @Pattern(regexp = "^[A-Z]{4}$", message = "Label d'équipe invalide")
        String teamLabel

) {}