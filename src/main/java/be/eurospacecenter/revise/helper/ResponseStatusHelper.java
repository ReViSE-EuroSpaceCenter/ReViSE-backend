package be.eurospacecenter.revise.helper;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class ResponseStatusHelper {

    private ResponseStatusHelper() {

    }

    public static ResponseStatusException badRequest(String message, Exception e) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message + " : " + e.getMessage(), e);
    }

    public static ResponseStatusException notFound(String message, Exception e) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, message + " : " + e.getMessage(), e);
    }

    public static ResponseStatusException forbidden(String message, Exception e) {
        return new ResponseStatusException(HttpStatus.FORBIDDEN, message + " : " + e.getMessage(), e);
    }
}
