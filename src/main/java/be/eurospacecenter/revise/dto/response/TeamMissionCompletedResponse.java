package be.eurospacecenter.revise.dto.response;

import be.eurospacecenter.revise.model.MissionType;

public record TeamMissionCompletedResponse(String teamLabel, MissionType missionType) {
}
