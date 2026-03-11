package be.eurospacecenter.revise.model.launcher;

import be.eurospacecenter.revise.exceptions.ErrorKeys;
import be.eurospacecenter.revise.exceptions.InvalidLauncherOperationException;

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

    public Map<ResourceType, Integer> update(Map<ResourceType, Integer> toRemove) {
        checkSufficientResources(toRemove);
        toRemove.forEach((type, amount) ->
                resourcesMap.merge(type, -amount, Integer::sum)
        );
        return Map.copyOf(resourcesMap);
    }

    private void checkSufficientResources(Map<ResourceType, Integer> toRemove) {
        toRemove.forEach((type, required) -> {
            int current = resourcesMap.getOrDefault(type, 0);
            if (current < required) {
                throw new InvalidLauncherOperationException(ErrorKeys.INSUFFICIENT_RESOURCES);
            }
        });
    }
}

