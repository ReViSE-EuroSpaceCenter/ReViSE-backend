package be.eurospacecenter.revise.model;

import be.eurospacecenter.revise.exceptions.InvalidGameOperationException;

import java.util.Map;
import java.util.UUID;

public class Game {

    private final Map<UUID, Team> teams;

    public Game(Map<UUID, Team> teams) {
        this.teams = teams;
    }

    public void completeTeamMission(UUID id, MissionType missionType, Map<ResourceType, Integer> resources) {
        try {
            Team team = teams.get(id);
            team.completeMission(missionType);
            removeRessources(team, resources);
        } catch (NullPointerException e) {
            throw new InvalidGameOperationException("Équipe introuvable");
        }
    }

    public int generalScore() {
        return teams.values().stream().mapToInt(Team::score).sum();
    }

    private void removeRessources(Team team, Map<ResourceType, Integer> resources) {
        try {
            for (ResourceType type : ResourceType.values()) {
                int amount = resources.getOrDefault(type, 0);
                team.remove(type, amount);
            }
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new InvalidGameOperationException("Impossible de retirer les ressources à l'équipe");
        }
    }
}
