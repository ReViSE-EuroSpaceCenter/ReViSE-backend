package be.eurospacecenter.revise.model;

import be.eurospacecenter.revise.model.resource.ResourceType;
import be.eurospacecenter.revise.model.resource.TeamResources;
import be.eurospacecenter.revise.model.lobby.TeamLabel;
import be.eurospacecenter.revise.model.mission.MissionType;
import be.eurospacecenter.revise.model.mission.Missions;
import be.eurospacecenter.revise.model.mission.TeamFullProgression;
import be.eurospacecenter.revise.model.mission.TeamProgression;

import java.util.Map;
import java.util.UUID;

public class Team {
    private final UUID clientId;
    private TeamLabel label;

    private final Missions missions;
    private final Resources resources;

    public Team(UUID clientId) {
        this.clientId = clientId;
        this.missions = new Missions();
        this.resources = new Resources();
    }

    public Team(TeamLabel label, UUID clientId) {
        this.label = label;
        this.clientId = clientId;
        this.missions = new Missions();
        this.resources = new Resources();
    }

    public TeamLabel getLabel() {
        return label != null ? label : null;
    }

    public void setLabel(TeamLabel label) {
        this.label = label;
    }

    public UUID getClientID() {
        return clientId;
    }

    public boolean hasLabel() {
        return label != null;
    }

    public void updateMission(MissionType mission) {
        missions.update(label, mission);
    }

    public boolean allClassicMissionsCompleted() {
        return missions.allClassicMissionsCompleted(label);
    }

    public TeamProgression getProgression() {
        return missions.getProgression(label);
    }

    public TeamFullProgression getFullProgression() {
        return missions.getFullProgression(label);
    }

    public TeamResources updateResources(Map<ResourceType, Integer> newResources) {
        Map<ResourceType, Integer> resourcesMap = this.resources.update(newResources);
        return new TeamResources(label, resourcesMap);
    }

    public int score() {
        return resources.score();
    }
}