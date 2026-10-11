package dev.abdulrahman.excelsior.presentation;

import dev.abdulrahman.excelsior.business.game.GameService;
import dev.abdulrahman.excelsior.business.game.dto.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;

@RestController
@Validated
@RequestMapping("/api/games")
public class GameController {
    private static final String INVALID_ID_MESSAGE = "Game ID must be greater than 0.";
    private final GameService service;

    public GameController(GameService service) {
        this.service = service;
    }

    @GetMapping("/search")
    public List<GameResponseDto> searchGame(
            @NotBlank(message = "Search query cannot be empty.")
            @RequestParam
            String query,

            @Positive(message = "Search result limit must be greater than 0.")
            @RequestParam(required = false)
            Integer limit,

            @Min(value = 0, message = "Search result offset must not be negative.")
            @RequestParam(required = false)
            Integer offset
    ) {
        return this.service.searchGame(query, limit, offset);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void addAllGames(@RequestBody Collection<@Valid GameRequestDto> dtos) {
        this.service.addAllGames(dtos);
    }

    @GetMapping("/stats")
    public GameStatsDto getStats() {
        return this.service.getStats();
    }

    @GetMapping
    public Page<GameResponseDto> getAllGames(@Valid GameFilterDto dto, Pageable pageable) {
        return this.service.findGamesDynamically(
                dto.minRating(), dto.maxRating(), dto.nameContains(), dto.genreContains(),
                dto.minLengthNormal(), dto.maxLengthNormal(), dto.minLengthComplete(), dto.maxLengthComplete(),
                dto.minDate(), dto.maxDate(), dto.minYear(), dto.maxYear(), 
                dto.tier(), dto.status(), dto.platformContains(), pageable
        );
    }

    @GetMapping("/{id}")
    public GameResponseDto getGame(@Positive(message = INVALID_ID_MESSAGE) @PathVariable Long id) {
        return this.service.getGameById(id);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateGame(
            @Positive(message = INVALID_ID_MESSAGE)
            @PathVariable
            Long id,

            @Valid
            @RequestBody
            GameRequestDto dto
    ) {
        this.service.updateGame(id, dto);
    }

    @PatchMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void patchGame(
            @Positive(message = INVALID_ID_MESSAGE)
            @PathVariable
            Long id,

            @Valid
            @RequestBody
            GamePatchDto dto
    ) {
        if (dto.tier() != null) {
            this.service.updateTier(id, dto.tier());
        }

        if (dto.status() != null) {
            this.service.updateStatus(id, dto.status());
        }
    }

    @PatchMapping("/tier-positions")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void patchTierPositions(@RequestBody Collection<@Valid GameTierPositionPatchDto> dtos) {
        service.updateTierPositionForAll(dtos);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteGame(@Positive(message = INVALID_ID_MESSAGE) @PathVariable Long id) {
        this.service.deleteGame(id);
    }
}
