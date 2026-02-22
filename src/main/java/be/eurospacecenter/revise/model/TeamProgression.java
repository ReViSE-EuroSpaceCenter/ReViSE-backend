package be.eurospacecenter.revise.model;

public record TeamProgression(
        float classicMissionPercentage,
        boolean firstBonusMissionCompleted,
        boolean secondBonusMissionCompleted
) {}
