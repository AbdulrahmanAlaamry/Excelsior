package dev.abdulrahman.excelsior.presentation;

import dev.abdulrahman.excelsior.business.GameService;
import dev.abdulrahman.excelsior.business.game.GameRequestDto;
import dev.abdulrahman.excelsior.business.game.GameResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/games")
public class GameController {
    private final GameService service;

    public GameController(GameService service) {
        this.service = service;
    }

    @PostMapping("/search")
    @ResponseStatus(HttpStatus.CREATED)
    public void searchAndAddGame(@RequestParam String query) {
        this.service.searchAndAddGame(query);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void addGame(@RequestBody GameRequestDto dto) {
        this.service.addGame(dto);
    }

    @GetMapping
    public Iterable<GameResponseDto> getAllGames() {
        return this.service.getAllGames();
    }

    @GetMapping("/{id}")
    public GameResponseDto getGame(@PathVariable Long id) {
        return this.service.getGameById(id);
    }

    @PutMapping("/{id}")
    public void updateGame(@PathVariable Long id, @RequestBody GameRequestDto dto) {
        this.service.updateGame(id, dto);
    }

    @DeleteMapping("/{id}")
    public void deleteGame(@PathVariable Long id) {
        this.service.deleteGame(id);
    }
}
