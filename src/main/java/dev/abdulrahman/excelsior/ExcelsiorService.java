package dev.abdulrahman.excelsior;

import dev.abdulrahman.excelsior.game.*;
import dev.abdulrahman.randomstringgenerator.RandomStringGenerator;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;

@Service
public class ExcelsiorService {
    private final Map<Long, Game> games;
    private final RandomStringGenerator generator;

    public ExcelsiorService(Map<Long, Game> games, RandomStringGenerator generator) {
        this.games = games; // Map implementation does not allow null values (ConcurrentSkipListMap)
        this.generator = generator;
    }

    public void addGame(GameDto dto) {
        Long id = Long.valueOf(generator.generate());
        this.games.put(id, new Game(id, dto));
    }

    public Collection<Game> getAllGames() {
        return this.games.values();
    }

    public Game getGameById(Long id) {
        Game game = games.get(id);
        if (game == null) {
            throw new GameNotFoundException();
        }
        return game;
    }

    public void updateGame(Long id, GameDto dto) {
        if (!this.games.containsKey(id)) {
            throw new GameNotFoundException();
        }
        this.games.put(id, new Game(id, dto));
    }

    public void deleteGame(Long id) {
        if (this.games.remove(id) == null) {
            throw new GameNotFoundException();
        }
    }
}
