package dev.abdulrahman.excelsior.integration;

import dev.abdulrahman.excelsior.business.game.GameResponseDto;
import dev.abdulrahman.excelsior.integration.dto.ImageDto;
import dev.abdulrahman.excelsior.integration.dto.GameDto;
import dev.abdulrahman.excelsior.integration.dto.TimeToBeatDto;
import dev.abdulrahman.excelsior.mapper.DtoMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;

import dev.abdulrahman.excelsior.integration.dto.*;
import java.util.List;
import dev.abdulrahman.excelsior.business.game.GameTier;
import dev.abdulrahman.excelsior.business.game.GameStatus;

class DtoMapperTest {

    private DtoMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new DtoMapper();
    }

    @Test
    void toGameResponseDto_AllFieldsPresent_MapsCorrectly() {
        ImageDto cover = new ImageDto("http://example.com/cover.jpg", 100, 200);
        Long releaseDateEpoch = Instant.parse("2023-10-15T00:00:00Z").getEpochSecond();
        EngineDto[] engines = new EngineDto[]{new EngineDto("Unreal Engine 5")};
        GenreDto[] genres = new GenreDto[]{new GenreDto("Action"), new GenreDto("Adventure")};
        InvolvedCompanyDto[] companies = new InvolvedCompanyDto[]{
                new InvolvedCompanyDto(new CompanyDto("Santa Monica Studio")),
                new InvolvedCompanyDto(new CompanyDto("Sony Interactive Entertainment"))
        };

        ImageDto[] screenshots = new ImageDto[]{new ImageDto("http://example.com/scr.jpg", 1, 1)};
        PlatformDto[] platforms = new PlatformDto[]{new PlatformDto("PC")};

        GameDto gameDto = new GameDto(1, cover, releaseDateEpoch, "Awesome Game", "awesome-game",
                "A great storyline", "A short summary", 85.5, 120, engines, genres, companies, screenshots, platforms);

        // normally = 36000 seconds (10 hours), completely = 72000 seconds (20 hours)
        TimeToBeatDto timeToBeatDto = new TimeToBeatDto(1, 72000, 36000);

        GameResponseDto result = mapper.toGameResponseDto(gameDto, false, timeToBeatDto);

        assertNotNull(result);
        assertEquals("Awesome Game", result.name());
        assertEquals(10.0, result.lengthNormal());
        assertEquals(20.0, result.lengthComplete());
        assertEquals(Instant.ofEpochSecond(releaseDateEpoch).atZone(ZoneId.systemDefault()).toLocalDate(), result.releaseDate());
        assertEquals("http://example.com/cover.jpg", result.coverUrl());
        assertEquals(85.5, result.rating());
        assertEquals(120, result.ratingCount());
        assertEquals("A great storyline", result.synopsis());
        assertEquals("A short summary", result.description());
        assertEquals("Unreal Engine 5", result.primaryEngine());
        assertEquals(List.of("Action", "Adventure"), result.genres());
        assertEquals(List.of("Santa Monica Studio", "Sony Interactive Entertainment"), result.companies());
        assertEquals(List.of("http://example.com/scr.jpg"), result.screenshotUrls());
        assertEquals(List.of("PC"), result.platforms());
    }

    @Test
    void toGameResponseDto_NullFields_MapsSafely() {
        GameDto gameDto = new GameDto(1, new ImageDto(null, null, null), null, null, null, null, null, null, null, null, null, null, null, null);
        TimeToBeatDto timeToBeatDto = new TimeToBeatDto(1, null, null);

        GameResponseDto result = mapper.toGameResponseDto(gameDto, false, timeToBeatDto);

        assertNotNull(result);
        assertNull(result.name());
        assertEquals(0.0, result.lengthNormal());
        assertEquals(0.0, result.lengthComplete());
        assertEquals(LocalDate.EPOCH, result.releaseDate());
        assertNull(result.coverUrl());
        assertNull(result.rating());
        assertNull(result.ratingCount());
        assertNull(result.synopsis());
        assertNull(result.description());
        assertEquals("Unknown/Proprietary", result.primaryEngine());
        assertEquals(List.of(), result.genres());
        assertEquals(List.of(), result.companies());
        assertEquals(List.of(), result.screenshotUrls());
        assertEquals(List.of(), result.platforms());
        assertEquals(GameTier.NONE, result.tier());
        assertEquals(GameStatus.UNKNOWN, result.status());
    }
}
