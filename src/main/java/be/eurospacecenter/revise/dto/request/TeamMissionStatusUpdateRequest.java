package be.eurospacecenter.revise.dto.request;

import be.eurospacecenter.revise.model.MissionType;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;


public record TeamMissionStatusUpdateRequest(

        @NotNull
        UUID clientId,

        @NotNull
        List<MissionType> updateMissions

) {}