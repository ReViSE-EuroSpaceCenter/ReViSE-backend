package be.eurospacecenter.revise.model;

import java.util.Map;

public record TeamFullProgression (Map<String, Boolean> completedMissions, TeamProgression teamProgression) {
}
