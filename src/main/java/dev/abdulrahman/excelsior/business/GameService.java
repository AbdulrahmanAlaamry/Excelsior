package dev.abdulrahman.excelsior.business;

import dev.abdulrahman.excelsior.business.game.*;
import dev.abdulrahman.excelsior.integration.ClientService;
import dev.abdulrahman.excelsior.mapper.DtoMapper;
import dev.abdulrahman.excelsior.persistence.GameRepository;
import org.springframework.stereotype.Service;

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
        addGame(clientService.searchGames(query));
    }

    public void addGame(GameRequestDto dto) {
        if (repo.existsByIgdbId(dto.igdbId())) {
            throw new GameAlreadyExistsException();
        }
        repo.save(mapper.toGameEntity(dto));
    }

    public List<GameResponseDto> getAllGames() {
        return repo.findAll().stream()
                .map(mapper::toGameResponseDto)
                .toList();
    }

    public GameResponseDto getGameById(Long id) {
        return mapper.toGameResponseDto(findGameById(id));
    }

    public void updateGame(Long id, GameRequestDto dto) {
        Game game = findGameById(id);
        game.setAll(dto);
        repo.save(game);
    }

    public void deleteGame(Long id) {
        repo.delete(findGameById(id));
    }

    private Game findGameById(Long id) {
        return repo.findById(id).orElseThrow(GameNotFoundException::new);
    }
}
