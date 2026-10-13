package dev.abdulrahman.excelsior.persistence;

import dev.abdulrahman.excelsior.business.game.Game;
import dev.abdulrahman.excelsior.business.game.GameStatus;
import dev.abdulrahman.excelsior.business.game.GameTier;
import dev.abdulrahman.excelsior.business.user.User;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class GameSpecifications {

    private GameSpecifications() {}

    public static Specification<Game> withDynamicFilter(
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
            String platformContains
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (user != null) {
                predicates.add(cb.equal(root.get("user"), user));
            }

            if (minRating != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("rating"), minRating));
            }

            if (maxRating != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("rating"), maxRating));
            }

            if (nameContains != null && !nameContains.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + nameContains.trim().toLowerCase() + "%"));
            }

            if (genreContains != null && !genreContains.isBlank()) {
                predicates.add(cb.isMember(genreContains, root.get("genres")));
            }

            if (minLengthNormal != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("lengthNormal"), minLengthNormal));
            }

            if (maxLengthNormal != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("lengthNormal"), maxLengthNormal));
            }

            if (minLengthComplete != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("lengthComplete"), minLengthComplete));
            }

            if (maxLengthComplete != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("lengthComplete"), maxLengthComplete));
            }

            if (minDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("releaseDate"), minDate));
            }

            if (maxDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("releaseDate"), maxDate));
            }

            if (minYear != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("releaseDate"), LocalDate.of(minYear, 1, 1)));
            }

            if (maxYear != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("releaseDate"), LocalDate.of(maxYear, 12, 31)));
            }

            if (tier != null) {
                predicates.add(cb.equal(root.get("tierValue"), tier));
            }

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            if (platformContains != null && !platformContains.isBlank()) {
                predicates.add(cb.isMember(platformContains, root.get("platforms")));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
