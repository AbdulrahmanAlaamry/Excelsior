package dev.abdulrahman.excelsior.game;

import java.time.LocalDate;

public record Game(Long id, String name, Double length, LocalDate releaseDate) {
    public Game(Long id, GameDto dto) {
        this(id, dto.name(), dto.length(), dto.releaseDate());
    }
}
