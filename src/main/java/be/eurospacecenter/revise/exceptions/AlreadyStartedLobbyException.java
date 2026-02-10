package be.eurospacecenter.revise.exceptions;


public class AlreadyStartedLobbyException extends RuntimeException {
    public AlreadyStartedLobbyException(String errorMessage) {
        super(errorMessage);
    }
}
