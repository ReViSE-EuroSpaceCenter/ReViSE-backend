package be.eurospacecenter.revise.service;

import java.util.List;

public interface Cleanable {
    void cleanUp(List<String> toRemove);
}
