package be.eurospacecenter.revise.helper;

import org.junit.jupiter.api.Test;

import java.security.SecureRandom;

import static org.junit.jupiter.api.Assertions.*;

class LobbyCodeTest {

    SecureRandom rand = new SecureRandom();

    @Test
    void generateLobbyCode() {
        String lobbyCode = LobbyCode.generateCode(rand);
        assertNotNull(lobbyCode);
        assertEquals(6, lobbyCode.length());
        assertTrue(lobbyCode.matches("[A-Z0-9]+"));
    }

    @Test
    void generateMultipleLobbyCodes() {
        for (int i = 0; i < 1000; i++) {
            String lobbyCode = LobbyCode.generateCode(rand);
            assertNotNull(lobbyCode);
            assertEquals(6, lobbyCode.length());
            assertTrue(lobbyCode.matches("[A-Z0-9]+"));
        }
    }

    @Test
    void initializeLobbyCodeClass() {
        assertThrows(IllegalStateException.class, LobbyCode::new);
    }
}