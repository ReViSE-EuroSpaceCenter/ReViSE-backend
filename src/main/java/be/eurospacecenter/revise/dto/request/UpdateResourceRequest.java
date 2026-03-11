package be.eurospacecenter.revise.dto.request;

import be.eurospacecenter.revise.model.launcher.ResourceType;

import java.util.Map;
import java.util.UUID;

public record UpdateResourceRequest(UUID clientId, Map<ResourceType, Integer> resources) { }
