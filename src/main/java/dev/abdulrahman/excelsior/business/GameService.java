package dev.abdulrahman.excelsior.business;

import dev.abdulrahman.excelsior.business.game.*;
import dev.abdulrahman.excelsior.integration.ClientService;
import dev.abdulrahman.excelsior.mapper.DtoMapper;
import dev.abdulrahman.excelsior.persistence.GameRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.function.BiConsumer;

@Service
public class GameService {
    private final GameRepository repo;
    private final ClientService clientService;
    private final DtoMapper mapper;

    public GameService(GameRepository repo, ClientService clientService, DtoMapper mapper) {
        this.repo = repo;
        this.clientService = clientService;
        this.mapper = mapper;
    }

    public void searchAndAddGame(String query) {
        addGame(searchGame(query));
    }

    public GameRequestDto searchGame(String query) {
        return clientService.searchGames(query);
    }

    public void addGame(GameRequestDto dto) {
        if (repo.existsByIgdbId(dto.igdbId())) {
            throw new GameAlreadyExistsException();
        }
        repo.save(mapper.toGameEntity(dto));
    }

    public GameResponseDto getGameById(Long id) {
        return mapper.toGameResponseDto(findGameById(id));
    }

    public void updateGame(Long id, GameRequestDto dto) {
        Game game = findGameById(id);
        game.setAll(dto);
        repo.save(game);
    }

    public void updateTier(Long id, GameTier tier) {
        updateField(id, Game::setTier, tier);
    }

    public void updateStatus(Long id, GameStatus status) {
        updateField(id, Game::setStatus, status);
    }

    public void deleteGame(Long id) {
        repo.delete(findGameById(id));
    }

    public List<GameResponseDto> findGamesDynamically(
            Double minRating, Double maxRating, String nameContains, String genreContains,
            Double minLengthNormal, Double maxLengthNormal, Double minLengthComplete, Double maxLengthComplete, 
            LocalDate minDate, LocalDate maxDate, Integer minYear, Integer maxYear, Sort sort
    ) {
        return mapper.toGameResponseDtoList(
                repo.findDynamically(
                        minRating, maxRating, nameContains, genreContains,
                        minLengthNormal, maxLengthNormal, minLengthComplete, maxLengthComplete, 
                        minDate, maxDate, minYear, maxYear, sort
                )
        );
    }

    private Game findGameById(Long id) {
        return repo.findById(id).orElseThrow(GameNotFoundException::new);
    }

    private <T> void updateField(Long id, BiConsumer<Game, T> mutator, T field) {
        Game game = findGameById(id);
        mutator.accept(game, field);
        repo.save(game);
    }
}
