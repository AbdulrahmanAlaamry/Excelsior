package dev.abdulrahman.excelsior.persistence;

import dev.abdulrahman.excelsior.business.game.Game;
import dev.abdulrahman.excelsior.business.game.GameStatus;
import dev.abdulrahman.excelsior.business.game.GameTier;
import dev.abdulrahman.excelsior.business.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface GameRepository extends JpaRepository<Game, Long>, JpaSpecificationExecutor<Game> {
    List<Game> findAllByUser(User user);
    Optional<Game> findByUserAndId(User user, Long id);
    Optional<Game> findByUserAndIgdbId(User user, Long igdbId);
    boolean existsByUserAndIgdbId(User user, Long igdbId);

    default Page<Game> findDynamically(
            User user,
            Double minRating,
            Double maxRating,
            String nameContains,
            String genreContains,
            Double minLengthNormal,
            Double maxLengthNormal,
            Double minLengthComplete,
            Double maxLengthComplete,
            LocalDate minDate,
            LocalDate maxDate,
            Integer minYear,
            Integer maxYear,
            GameTier tier,
            GameStatus status,
            String platformContains,
            Pageable pageable
    ) {
        Specification<Game> spec = GameSpecifications.withDynamicFilter(
                user, minRating, maxRating, nameContains, genreContains,
                minLengthNormal, maxLengthNormal, minLengthComplete, maxLengthComplete,
                minDate, maxDate, minYear, maxYear, tier, status, platformContains
        );
        return findAll(spec, pageable);
    }
}
