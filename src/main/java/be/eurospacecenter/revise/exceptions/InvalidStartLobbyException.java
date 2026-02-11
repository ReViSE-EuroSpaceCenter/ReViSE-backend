package be.eurospacecenter.revise.exceptions;

public class InvalidStartLobbyException extends RuntimeException {
    public InvalidStartLobbyException(String errorMessage) {
        super(errorMessage);
    }
}
