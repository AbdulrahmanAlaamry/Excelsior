package dev.abdulrahman.excelsior.presentation.exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.*;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    @ExceptionHandler(BadCredentialsException.class)
    public ProblemDetail handleBadCredentialsException(BadCredentialsException ex) {
        return handleException(
                HttpStatus.UNAUTHORIZED,
                "Invalid username or password",
                "Unauthorized"
        );
    }

    @ExceptionHandler(GameNotFoundException.class)
    public ProblemDetail handleGameNotFound(GameNotFoundException ex) {
        return handleException(ex, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(GameAlreadyExistsException.class)
    public ProblemDetail handleGameAlreadyExists(GameAlreadyExistsException ex) {
        return handleException(ex, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleConstraintViolation(ConstraintViolationException ex) {
        return handleException(
                HttpStatus.BAD_REQUEST,
                ex.getMessage().replaceFirst("^\\S+\\s+", ""), // Strip method name
                "Validation Error"
        );
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ProblemDetail handleUserNotFoundException(UserNotFoundException ex) {
        return handleException(ex, HttpStatus.NOT_FOUND);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        String detail = ex.getBindingResult().getFieldErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .findFirst()
                .orElse("Validation failed");

        ProblemDetail problemDetail = handleException(
                HttpStatus.BAD_REQUEST,
                detail,
                "Validation Error"
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
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
