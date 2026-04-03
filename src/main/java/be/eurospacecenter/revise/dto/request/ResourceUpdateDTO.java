package be.eurospacecenter.revise.dto.request;

import be.eurospacecenter.revise.model.discover.ResourceType;

import java.util.Map;
import java.util.UUID;

public record ResourceUpdateDTO(UUID clientId, Map<ResourceType, Integer> resources) { }
