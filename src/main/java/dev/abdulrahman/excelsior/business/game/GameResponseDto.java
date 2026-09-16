package dev.abdulrahman.excelsior.business.game;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public record GameResponseDto(
        Long id,
        String name,
        Double length,
        @JsonFormat(pattern = "dd/MM/yyyy")
        LocalDate releaseDate
) {
    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return name +
                "\nID: " + id +
                "\nLength: " + length +
                "\nRelease Date: " + releaseDate.format(formatter);
    }
}
