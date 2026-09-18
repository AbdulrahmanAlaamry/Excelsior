package dev.abdulrahman.excelsior.business.game;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class GameAlreadyExistsException extends RuntimeException {
    public GameAlreadyExistsException() {
        super();
    }
    public GameAlreadyExistsException(String message) {
        super(message);
    }
}
