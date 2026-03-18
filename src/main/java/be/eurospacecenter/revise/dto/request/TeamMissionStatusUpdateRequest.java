package be.eurospacecenter.revise.dto.request;

import be.eurospacecenter.revise.model.lobby.TeamLabel;
import be.eurospacecenter.revise.model.mission.MissionType;
import jakarta.validation.constraints.NotNull;

import java.util.Set;
import java.util.UUID;


public record TeamMissionStatusUpdateRequest(

        @NotNull
        UUID id,

        @NotNull
        Set<MissionType> updateMissions,

        TeamLabel teamLabel
) {}