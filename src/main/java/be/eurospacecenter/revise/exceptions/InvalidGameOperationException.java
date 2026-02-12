package be.eurospacecenter.revise.exceptions;

public class InvalidGameOperationException extends RuntimeException {
    public InvalidGameOperationException(String message) {
        super(message);
    }
}
