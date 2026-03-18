package be.eurospacecenter.revise.dto.request;

import be.eurospacecenter.revise.model.lobby.TeamLabel;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AssignTeamRequest(

        @NotNull
        UUID clientId,

        @NotNull
        TeamLabel teamLabel

) {}