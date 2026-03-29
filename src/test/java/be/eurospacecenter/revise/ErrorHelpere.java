package be.eurospacecenter.revise;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ErrorHelpere {
    public static void assertError(Class<? extends Exception> expectedType, String expectedKey, Runnable action) {
        Exception ex = assertThrows(expectedType, action::run);
        assertEquals(expectedKey, ex.getMessage());
    }
}
