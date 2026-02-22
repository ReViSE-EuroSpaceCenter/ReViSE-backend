package be.eurospacecenter.revise.model;

import be.eurospacecenter.revise.exceptions.NotFoundException;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class Game {

    private final Map<UUID, Team> teams;

    public Game(Map<UUID, Team> teams) {
        this.teams = teams;
    }
    
    public String getTeamLabel(UUID id) {
        return teams.get(id).getLabel();
    }

    public void changeTeamMissionState(UUID id, MissionType missionType) {
        Team team = getTeam(id);
        team.changeMissionState(missionType);
    }

    public TeamProgression getTeamProgression(UUID id) {
        Team team = getTeam(id);
        return team.getProgression();
    }

    public Team getTeam(UUID id) {
        return Optional.ofNullable(teams.get(id)).orElseThrow(() -> new NotFoundException("Équipe introuvable"));
    }
}
