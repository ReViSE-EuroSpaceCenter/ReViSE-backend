package be.eurospacecenter.revise.exceptions;

import be.eurospacecenter.revise.model.discover.ResourceType;
import be.eurospacecenter.revise.model.mission.MissionType;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import tools.jackson.databind.exc.InvalidFormatException;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({IllegalArgumentException.class, InvalidMissionOperationException.class, NotFoundException.class, NoAutoriseOperationException.class})
    public ProblemDetail handleGameExceptions(RuntimeException ex) {

        HttpStatus status = switch (ex) {
            case NotFoundException e -> HttpStatus.NOT_FOUND;
            case NoAutoriseOperationException e -> HttpStatus.FORBIDDEN;
            default -> HttpStatus.BAD_REQUEST;
        };

        return buildProblemDetail(
                status,
                ex.getClass().getSimpleName(),
                ex.getMessage()
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleConstraintViolation(
            ConstraintViolationException ex) {

        String message = ex.getConstraintViolations()
                .stream()
                .findFirst()
                .map(ConstraintViolation::getMessage)
                .orElse("Validation error");

        ProblemDetail problem =
                ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);

        problem.setTitle("Validation Failed");
        problem.setDetail(message);

        return problem;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleJsonParseError(HttpMessageNotReadableException ex) {

        ProblemDetail problem =
                ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);

        problem.setTitle("Invalid request body");

        Throwable cause = ex.getMostSpecificCause();

        if (cause instanceof InvalidFormatException invalidFormatException) {

            String errorCode =
                    TYPE_ERROR_MAP.get(invalidFormatException.getTargetType());

            if (errorCode != null) {
                problem.setDetail(errorCode);
                return problem;
            }
        }

        problem.setDetail(cause.getMessage());

        return problem;
    }

    @ExceptionHandler(InvalidGameStateException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidState(InvalidGameStateException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", ErrorKeys.INVALID_GAME_STATE);
        body.put("message", "The action cannot be performed in the current game state.");
        body.put("currentState", ex.getCurrentState());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    private ProblemDetail buildProblemDetail(
            HttpStatus status,
            String title,
            String detail) {

        ProblemDetail problem = ProblemDetail.forStatus(status);
        problem.setTitle(title);
        problem.setDetail(detail);
        return problem;
    }

    private static final Map<Class<?>, String> TYPE_ERROR_MAP = Map.of(
            UUID.class, ErrorKeys.INVALID_UUID,
            MissionType.class, ErrorKeys.INVALID_MISSION_TYPE,
            ResourceType.class, ErrorKeys.INVALID_RESOURCE_TYPE
    );
}