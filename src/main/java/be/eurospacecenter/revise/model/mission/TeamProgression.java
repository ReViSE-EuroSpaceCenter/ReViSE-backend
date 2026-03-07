package be.eurospacecenter.revise.model.mission;

public record TeamProgression(
        int classicMissionsCompleted,
        boolean firstBonusMissionCompleted,
        boolean secondBonusMissionCompleted
) {}
