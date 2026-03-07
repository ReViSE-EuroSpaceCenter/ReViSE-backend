package be.eurospacecenter.revise.dto.request;

import be.eurospacecenter.revise.model.mission.MissionType;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;


public record TeamMissionStatusUpdateRequest(

        @NotNull
        UUID clientId,

        @NotNull
        MissionType missionNumber

) {}