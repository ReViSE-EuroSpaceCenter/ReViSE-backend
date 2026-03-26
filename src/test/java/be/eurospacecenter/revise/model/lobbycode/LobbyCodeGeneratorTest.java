package be.eurospacecenter.revise.model.lobbycode;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LobbyCodeGeneratorTest {

    LobbyCodeGenerator lobbyCodeGenerator = new LobbyCodeGenerator();

    @Test
    void generateLobbyCode() {
        LobbyCode lobbyCode = lobbyCodeGenerator.generate();
        assertNotNull(lobbyCode);
        assertEquals(6, lobbyCode.lobbyCode().length());
        assertTrue(lobbyCode.lobbyCode().matches("[A-Z0-9]+"));
    }

    @Test
    void generateMultipleLobbyCodes() {
        for (int i = 0; i < 1000; i++) {
            LobbyCode lobbyCode = lobbyCodeGenerator.generate();
            assertNotNull(lobbyCode);
            assertEquals(6, lobbyCode.lobbyCode().length());
            assertTrue(lobbyCode.lobbyCode().matches("[A-Z0-9]+"));
        }
    }
}