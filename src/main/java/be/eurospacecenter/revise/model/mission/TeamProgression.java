package be.eurospacecenter.revise.model.mission;

import be.eurospacecenter.revise.model.lobby.TeamLabel;

public record TeamProgression(
        TeamLabel teamLabel,
        int classicMissionsCompleted,
        boolean firstBonusMissionCompleted,
        boolean secondBonusMissionCompleted
) {}
