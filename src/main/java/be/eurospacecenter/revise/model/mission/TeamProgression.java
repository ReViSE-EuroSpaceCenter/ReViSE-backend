package be.eurospacecenter.revise.model.mission;

public record TeamProgression(
        String teamLabel,
        int classicMissionsCompleted,
        boolean firstBonusMissionCompleted,
        boolean secondBonusMissionCompleted
) {}
