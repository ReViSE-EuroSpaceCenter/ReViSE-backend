package be.eurospacecenter.revise.service;

import org.springframework.stereotype.Service;

@Service
public class GreetingService {
    public String getMessage(String name) {
        return String.format("Greeting %s!", name);
    }
}
