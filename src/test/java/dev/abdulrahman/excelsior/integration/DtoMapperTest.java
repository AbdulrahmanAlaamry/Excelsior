package dev.abdulrahman.excelsior.integration;

import dev.abdulrahman.excelsior.business.game.GameRequestDto;
import dev.abdulrahman.excelsior.integration.dto.CoverDto;
import dev.abdulrahman.excelsior.integration.dto.GameDto;
import dev.abdulrahman.excelsior.integration.dto.TimeToBeatDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;

class DtoMapperTest {

    private DtoMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new DtoMapper();
    }

    @Test
    void toGameRequestDto_AllFieldsPresent_MapsCorrectly() {
        CoverDto cover = new CoverDto("http://example.com/cover.jpg", 100, 200);
        Long releaseDateEpoch = Instant.parse("2023-10-15T00:00:00Z").getEpochSecond();
        GameDto gameDto = new GameDto(1, cover, releaseDateEpoch, "Awesome Game", "awesome-game",
                "A great storyline", "A short summary", 85.5, 120);

        // normally = 36000 seconds (10 hours), completely = 72000 seconds (20 hours)
        TimeToBeatDto timeToBeatDto = new TimeToBeatDto(72000, 36000);

        GameRequestDto result = mapper.toGameRequestDto(gameDto, timeToBeatDto);

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
    }

    @Test
    void toGameRequestDto_NullFields_MapsSafely() {
        GameDto gameDto = new GameDto(1, new CoverDto(null, null, null), null, null, null, null, null, null, null);
        TimeToBeatDto timeToBeatDto = new TimeToBeatDto(null, null);

        GameRequestDto result = mapper.toGameRequestDto(gameDto, timeToBeatDto);

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
    }
}
