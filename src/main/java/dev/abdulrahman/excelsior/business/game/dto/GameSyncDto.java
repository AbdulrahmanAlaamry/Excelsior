package dev.abdulrahman.excelsior.business.game.dto;

import java.time.LocalDate;
import java.util.List;

public record GameSyncDto(
        String name,
        Double lengthNormal,
        Double lengthComplete,
        LocalDate releaseDate,
        String coverUrl,
        Double rating,
        Integer ratingCount,
        String synopsis,
        String description,
        String primaryEngine,
        List<String> genres,
        List<String> companies,
        List<String> screenshotUrls,
        List<String> platforms
) {
}
