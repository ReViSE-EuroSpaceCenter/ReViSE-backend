package be.eurospacecenter.revise.model.lobbycode;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class LobbyCodeGenerator {
    private final SecureRandom random = new SecureRandom();

    public LobbyCode generate() {
        String code = random.ints(6, 'A', 'Z' + 1)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();

        return new LobbyCode(code);
    }
}
