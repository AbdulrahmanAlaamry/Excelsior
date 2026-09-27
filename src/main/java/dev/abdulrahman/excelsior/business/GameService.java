package dev.abdulrahman.excelsior.business;

import dev.abdulrahman.excelsior.business.game.*;
import dev.abdulrahman.excelsior.integration.ClientService;
import dev.abdulrahman.excelsior.mapper.DtoMapper;
import dev.abdulrahman.excelsior.persistence.GameRepository;
import dev.abdulrahman.excelsior.presentation.exception.GameAlreadyExistsException;
import dev.abdulrahman.excelsior.presentation.exception.GameNotFoundException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;

@Service
public class GameService {
    private final GameRepository repository;
    private final ClientService clientService;
    private final DtoMapper mapper;

    public GameService(GameRepository repository, ClientService clientService, DtoMapper mapper) {
        this.repository = repository;
        this.clientService = clientService;
        this.mapper = mapper;
    }

    public List<GameResponseDto> searchGame(String query, Integer limit) {
        return clientService.searchGames(query, limit);
    }

    public void addGame(GameRequestDto dto) {
        if (repository.existsByIgdbId(dto.igdbId())) {
            throw new GameAlreadyExistsException("Game with IGDB ID '" + dto.igdbId() +
                    "' (" + dto.name() + ") already exists");
        }
        repository.save(mapper.toGameEntity(dto));
    }

    public void addAllGames(Collection<GameRequestDto> dtos) {
        dtos.forEach(this::addGame);
    }

    public GameResponseDto getGameById(Long id) {
        return mapper.toGameResponseDto(findGameById(id));
    }

    public void updateGame(Long id, GameRequestDto dto) {
        Game game = findGameById(id);
        game.setAll(dto);
        repository.save(game);
    }

    public void updateTier(Long id, GameTier tier) {
        updateField(id, Game::setTierValue, tier);
    }

    public void updateStatus(Long id, GameStatus status) {
        updateField(id, Game::setStatus, status);
    }

    public void deleteGame(Long id) {
        if (!repository.existsById(id)) {
            throw idNotFound(id);
        }
        repository.delete(findGameById(id));
    }

    public List<GameResponseDto> findGamesDynamically(
            Double minRating, Double maxRating, String nameContains, String genreContains,
            Double minLengthNormal, Double maxLengthNormal, Double minLengthComplete, Double maxLengthComplete, 
            LocalDate minDate, LocalDate maxDate, Integer minYear, Integer maxYear, Pageable pageable
    ) {
        return mapper.toGameResponseDtoList(
                repository.findDynamically(
                        minRating, maxRating, nameContains, genreContains,
                        minLengthNormal, maxLengthNormal, minLengthComplete, maxLengthComplete, 
                        minDate, maxDate, minYear, maxYear, pageable
                )
        );
    }

    private GameNotFoundException idNotFound(Long id) {
        return new GameNotFoundException("Could not find a game with ID '" + id + "'");
    }

    private Game findGameById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> idNotFound(id));
    }

    private <T> void updateField(Long id, BiConsumer<Game, T> mutator, T field) {
        Game game = findGameById(id);
        mutator.accept(game, field);
        repository.save(game);
    }
}
