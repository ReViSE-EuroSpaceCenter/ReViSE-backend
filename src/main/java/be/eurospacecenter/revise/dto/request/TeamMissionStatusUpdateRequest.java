package be.eurospacecenter.revise.dto.request;

import be.eurospacecenter.revise.model.mission.MissionType;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;


public record TeamMissionStatusUpdateRequest(

        @NotNull
        UUID id,

        @NotNull
        List<MissionType> updateMissions,

        String teamLabel
) {}