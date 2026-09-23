package dev.abdulrahman.excelsior.presentation.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    @ExceptionHandler(GameNotFoundException.class)
    public ProblemDetail handleGameNotFound(GameNotFoundException ex) {
        return handleException(ex, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(GameAlreadyExistsException.class)
    public ProblemDetail handleGameAlreadyExists(GameAlreadyExistsException ex) {
        return handleException(ex, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleAllUncaughtExceptions(Exception ex) {
        System.err.println("CRITICAL UNEXPECTED ERROR: " + ex.getMessage());
        ex.printStackTrace();

        String detail = "An unexpected error occurred on our end. Please try again later.";
        String title = "Internal Server Error";

        return handleException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                detail,
                title
        );
    }

    private ProblemDetail handleException(Exception ex, HttpStatus status) {
        return handleException(
                status,
                ex.getMessage(),
                getReadableExceptionName(ex)
        );
    }

    private ProblemDetail handleException(HttpStatus status, String detail, String title) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                status,
                detail
        );

        problemDetail.setTitle(title);
        problemDetail.setProperty("timestamp", Instant.now());

        return problemDetail;
    }

    private String getReadableExceptionName(Exception ex) {
        return ex.getClass().getSimpleName()
                .replaceAll("(?<!^)(?=[A-Z][a-z])", " ")
                .replaceFirst("\\s+Exception$", "");
    }
}
