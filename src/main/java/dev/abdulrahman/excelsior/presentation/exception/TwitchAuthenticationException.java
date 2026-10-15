package dev.abdulrahman.excelsior.presentation.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_GATEWAY)
public class TwitchAuthenticationException extends RuntimeException {
    public TwitchAuthenticationException() {
        super();
    }
    public TwitchAuthenticationException(String message) {
        super(message);
    }
}
