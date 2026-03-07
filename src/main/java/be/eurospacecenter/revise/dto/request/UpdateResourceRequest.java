package be.eurospacecenter.revise.dto.request;

import java.util.UUID;

public record UpdateResourceRequest(UUID clientId, String resourceName) {
}
