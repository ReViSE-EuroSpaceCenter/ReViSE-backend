package be.eurospacecenter.revise.model;

import be.eurospacecenter.revise.exceptions.ErrorKeys;
import be.eurospacecenter.revise.exceptions.InvalidDiscoverOperationException;
import be.eurospacecenter.revise.model.resource.ResourceType;

import java.util.EnumMap;
import java.util.Map;

public class Resources {

    private final Map<ResourceType, Integer> resourcesMap = new EnumMap<>(ResourceType.class);

    public Resources() {
        for (ResourceType type : ResourceType.values()) {
            resourcesMap.put(type, type.getMax());
        }
    }

    public int score() {
        return resourcesMap.get(ResourceType.ENERGY) / 3
                + resourcesMap.get(ResourceType.HUMAN)
                + resourcesMap.get(ResourceType.CLOCK);
    }

    public Map<ResourceType, Integer> update(Map<ResourceType, Integer> newResources) {
        checkSufficientResources(newResources);
        resourcesMap.putAll(newResources);
        return Map.copyOf(resourcesMap);
    }

    private void checkSufficientResources(Map<ResourceType, Integer> newResources) {
        newResources.forEach((type, newAmount) -> {
            int current = resourcesMap.getOrDefault(type, 0);
            if (current < newAmount) {
                throw new InvalidDiscoverOperationException(ErrorKeys.INSUFFICIENT_RESOURCES);
            }
        });
    }
}

