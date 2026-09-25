package dev.abdulrahman.excelsior.presentation;

import com.fasterxml.jackson.annotation.JsonFormat;
import dev.abdulrahman.excelsior.business.GameService;
import dev.abdulrahman.excelsior.business.game.*;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/games")
public class GameController {
    private final GameService service;

    public GameController(GameService service) {
        this.service = service;
    }

    @GetMapping("/search")
    public List<GameRequestDto> searchGame(@RequestParam String query, @RequestParam(required = false) Integer limit) {
        return this.service.searchGame(query, limit);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void addGame(@RequestBody GameRequestDto dto) {
        this.service.addGame(dto);
    }

    @GetMapping
    public List<GameResponseDto> getAllGames(
            @RequestParam(name = "min_rating", required = false) Double minRating,
            @RequestParam(name = "max_rating", required = false) Double maxRating,
            @RequestParam(name = "name_contains", required = false) String nameContains,
            @RequestParam(name = "genre_contains", required = false) String genreContains,
            @RequestParam(name = "min_length_normal", required = false) Double minLengthNormal,
            @RequestParam(name = "max_length_normal", required = false) Double maxLengthNormal,
            @RequestParam(name = "min_length_complete", required = false) Double minLengthComplete,
            @RequestParam(name = "max_length_complete", required = false) Double maxLengthComplete,
            @RequestParam(name = "min_date", required = false) @JsonFormat(pattern = "dd/MM/yyyy") LocalDate minDate,
            @RequestParam(name = "max_date", required = false) @JsonFormat(pattern = "dd/MM/yyyy") LocalDate maxDate,
            @RequestParam(name = "min_year", required = false) Integer minYear,
            @RequestParam(name = "max_year", required = false) Integer maxYear,
            Sort sort
            ) {

        return this.service.findGamesDynamically(
                minRating, maxRating, nameContains, genreContains,
                minLengthNormal, maxLengthNormal, minLengthComplete, maxLengthComplete,
                minDate, maxDate, minYear, maxYear, sort
        );
    }

    @GetMapping("/{id}")
    public GameResponseDto getGame(@PathVariable Long id) {
        return this.service.getGameById(id);
    }

    @PutMapping("/{id}")
    public void updateGame(@PathVariable Long id, @RequestBody GameRequestDto dto) {
        this.service.updateGame(id, dto);
    }

    @PatchMapping("/{id}")
    public void updateTier(@PathVariable Long id, @RequestBody GamePatchDto dto) {
        if (dto.tier() != null) {
            this.service.updateTier(id, dto.tier());
        }

        if (dto.status() != null) {
            this.service.updateStatus(id, dto.status());
        }
    }

    @DeleteMapping("/{id}")
    public void deleteGame(@PathVariable Long id) {
        this.service.deleteGame(id);
    }
}
