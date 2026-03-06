package be.eurospacecenter.revise.model;

public record TeamProgression(
        int classicMissionsCompleted,
        boolean firstBonusMissionCompleted,
        boolean secondBonusMissionCompleted
) {}
