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
            removeRessources(team, resources);
            team.completeMission(missionType);
        } catch (NullPointerException e) {
            throw new InvalidGameOperationException("Équipe introuvable");
        } catch (IllegalArgumentException e) {
            throw new InvalidGameOperationException("Numéro de mission invalide");
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
        } catch (IllegalArgumentException e) {
            throw new InvalidGameOperationException("Impossible de retirer les ressources à l'équipe");
        }
    }
}
