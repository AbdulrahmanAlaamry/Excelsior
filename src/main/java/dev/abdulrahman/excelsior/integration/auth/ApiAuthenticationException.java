package dev.abdulrahman.excelsior.integration.auth;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_GATEWAY)
public class ApiAuthenticationException extends RuntimeException {
    public ApiAuthenticationException() {
        super();
    }
    public ApiAuthenticationException(String message) {
        super(message);
    }
}
