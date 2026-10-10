package dev.abdulrahman.excelsior.persistence;

import dev.abdulrahman.excelsior.business.game.Game;
import dev.abdulrahman.excelsior.business.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface GameRepository extends JpaRepository<Game, Long> {
    List<Game> findAllByUser(User user);
    Optional<Game> findByUserAndId(User user, Long id);
    Optional<Game> findByUserAndIgdbId(User user, Long igdbId);
    boolean existsByUserAndIgdbId(User user, Long igdbId);

    @Query("SELECT g FROM Game g WHERE g.user = :user AND" +
           "(:minRating IS NULL OR g.rating >= :minRating) AND " +
           "(:maxRating IS NULL OR g.rating <= :maxRating) AND " +
           "(:nameContains IS NULL OR LOWER(g.name) LIKE LOWER(CONCAT('%', :nameContains, '%'))) AND " +
           "(:genreContains IS NULL OR :genreContains MEMBER OF g.genres) AND " +
           "(:minLengthNormal IS NULL OR g.lengthNormal >= :minLengthNormal) AND " +
           "(:maxLengthNormal IS NULL OR g.lengthNormal <= :maxLengthNormal) AND " +
           "(:minLengthComplete IS NULL OR g.lengthComplete >= :minLengthComplete) AND " +
           "(:maxLengthComplete IS NULL OR g.lengthComplete <= :maxLengthComplete) AND " +
           "(:minDate IS NULL OR g.releaseDate >= :minDate) AND " +
           "(:maxDate IS NULL OR g.releaseDate <= :maxDate) AND " +
           "(:minYear IS NULL OR YEAR(g.releaseDate) >= :minYear) AND " +
           "(:maxYear IS NULL OR YEAR(g.releaseDate) <= :maxYear) AND " +
           "(:tier IS NULL OR g.tierValue = :tier) AND " +
           "(:status IS NULL OR g.status = :status) AND " +
           "(:platformContains IS NULL OR :platformContains MEMBER OF g.platforms)")
    Page<Game> findDynamically(
            @Param("user") User user,
            @Param("minRating") Double minRating,
            @Param("maxRating") Double maxRating,
            @Param("nameContains") String nameContains,
            @Param("genreContains") String genreContains,
            @Param("minLengthNormal") Double minLengthNormal,
            @Param("maxLengthNormal") Double maxLengthNormal,
            @Param("minLengthComplete") Double minLengthComplete,
            @Param("maxLengthComplete") Double maxLengthComplete,
            @Param("minDate") LocalDate minDate,
            @Param("maxDate") LocalDate maxDate,
            @Param("minYear") Integer minYear,
            @Param("maxYear") Integer maxYear,
            @Param("tier") dev.abdulrahman.excelsior.business.game.GameTier tier,
            @Param("status") dev.abdulrahman.excelsior.business.game.GameStatus status,
            @Param("platformContains") String platformContains,
            Pageable pageable
    );
}
