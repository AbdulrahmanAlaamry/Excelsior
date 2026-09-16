package dev.abdulrahman.excelsior.business;

import dev.abdulrahman.excelsior.business.game.*;
import dev.abdulrahman.excelsior.persistence.GameRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GameService {
    private final GameRepository repo;

    public GameService(GameRepository repo) {
        this.repo = repo;
    }

    public void addGame(GameRequestDto dto) {
        repo.save(toEntity(dto));
    }

    public List<GameResponseDto> getAllGames() {
        return repo.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    public GameResponseDto getGameById(Long id) {
        return toDto(findGameById(id));
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

    private Game toEntity(GameRequestDto dto) {
        return new Game(dto.name(), dto.length(), dto.releaseDate());
    }

    private GameResponseDto toDto(Game game) {
        return new GameResponseDto(game.getId(), game.getName(), game.getLength(), game.getReleaseDate());
    }
}
