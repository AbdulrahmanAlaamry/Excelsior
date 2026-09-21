package dev.abdulrahman.excelsior.business;

import dev.abdulrahman.excelsior.business.game.*;
import dev.abdulrahman.excelsior.integration.ClientService;
import dev.abdulrahman.excelsior.mapper.DtoMapper;
import dev.abdulrahman.excelsior.persistence.GameRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

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

    @Deprecated(forRemoval = true)
    public List<GameResponseDto> getAllGames() {
        return mapper.toGameResponseDtoList(repo.findAll());
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
        Game game = findGameById(id);
        game.setTier(tier);
        repo.save(game);
    }

    public void deleteGame(Long id) {
        repo.delete(findGameById(id));
    }

    public List<GameResponseDto> findGamesDynamically(
            Double minRating, Double maxRating, String nameContains, String genreContains,
            Double minLengthNormal, Double maxLengthNormal, Double minLengthComplete, Double maxLengthComplete, 
            LocalDate minDate, LocalDate maxDate, org.springframework.data.domain.Sort sort
    ) {
        return mapper.toGameResponseDtoList(
                repo.findDynamically(
                        minRating, maxRating, nameContains, genreContains,
                        minLengthNormal, maxLengthNormal, minLengthComplete, maxLengthComplete, 
                        minDate, maxDate, sort
                )
        );
    }

    private Game findGameById(Long id) {
        return repo.findById(id).orElseThrow(GameNotFoundException::new);
    }
}
