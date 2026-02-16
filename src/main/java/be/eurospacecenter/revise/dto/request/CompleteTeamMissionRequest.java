package be.eurospacecenter.revise.dto.request;

import be.eurospacecenter.revise.model.MissionType;
import be.eurospacecenter.revise.model.ResourceType;
import jakarta.validation.constraints.NotNull;

import java.util.Map;
import java.util.UUID;


public record CompleteTeamMissionRequest(

        @NotNull
        UUID clientId,

        @NotNull
        MissionType missionNumber,

        @NotNull
        Map<ResourceType, Integer> resources

) {}