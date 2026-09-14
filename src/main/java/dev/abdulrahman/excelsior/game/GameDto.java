package dev.abdulrahman.excelsior.game;

import java.time.LocalDate;

public record GameDto(String name, Double length, LocalDate releaseDate) {
}
