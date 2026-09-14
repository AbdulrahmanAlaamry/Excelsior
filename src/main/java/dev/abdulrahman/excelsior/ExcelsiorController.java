package dev.abdulrahman.excelsior;

import dev.abdulrahman.excelsior.game.Game;
import dev.abdulrahman.excelsior.game.GameDto;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@RestController
@RequestMapping("/api/games")
public class ExcelsiorController {
    private final ExcelsiorService service;

    public ExcelsiorController(ExcelsiorService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void addGame(@RequestBody GameDto dto) {
        this.service.addGame(dto);
    }

    @GetMapping
    public Collection<Game> getAllGames() {
        return this.service.getAllGames();
    }

    @GetMapping("/{id}")
    public Game getGame(@PathVariable Long id) {
        return this.service.getGameById(id);
    }

    @PutMapping("/{id}")
    public void updateGame(@PathVariable Long id, @RequestBody GameDto dto) {
        this.service.updateGame(id, dto);
    }

    @DeleteMapping("/{id}")
    public void deleteGame(@PathVariable Long id) {
        this.service.deleteGame(id);
    }
}
