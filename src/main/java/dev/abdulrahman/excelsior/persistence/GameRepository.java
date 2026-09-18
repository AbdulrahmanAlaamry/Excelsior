package dev.abdulrahman.excelsior.persistence;

import dev.abdulrahman.excelsior.business.game.Game;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface GameRepository extends CrudRepository<Game, Long> {
    List<Game> findAll();
    boolean existsByIgdbId(Long id);
}
