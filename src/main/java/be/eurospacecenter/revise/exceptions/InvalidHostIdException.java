package be.eurospacecenter.revise.exceptions;

public class InvalidHostIdException extends RuntimeException {
    public InvalidHostIdException(String errorMessage) {
        super(errorMessage);
    }
}
