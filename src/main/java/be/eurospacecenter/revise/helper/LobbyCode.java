package be.eurospacecenter.revise.helper;

import java.security.SecureRandom;

public class LobbyCode {

    LobbyCode() {
        throw new IllegalStateException("Utility class");
    }

    public static String generateCode(SecureRandom random) {
        return random.ints(6, 'A', 'Z' + 1).collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append).toString();
    }
}
