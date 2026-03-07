package be.eurospacecenter.revise.model.mission;

public record TeamProgression(
        float classicMissionPercentage,
        boolean firstBonusMissionCompleted,
        boolean secondBonusMissionCompleted
) {}
