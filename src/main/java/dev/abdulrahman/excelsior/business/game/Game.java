package dev.abdulrahman.excelsior.business.game;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

@Entity
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private Double lengthNormal;
    private Double lengthComplete;
    private LocalDate releaseDate;
    private String coverUrl;
    private Double rating;
    private Integer ratingCount;
    @Column(length = 5000) private String synopsis;
    @Column(length = 5000) private String description;

    protected Game() {}

    public Game(String name,
                Double lengthNormal,
                Double lengthComplete,
                LocalDate releaseDate,
                String coverUrl,
                Double rating,
                Integer ratingCount,
                String synopsis,
                String description
    ) {
        this.name = name;
        this.lengthNormal = lengthNormal;
        this.lengthComplete = lengthComplete;
        this.releaseDate = releaseDate;
        this.coverUrl = coverUrl;
        this.rating = rating;
        this.ratingCount = ratingCount;
        this.synopsis = synopsis;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getLengthNormal() {
        return lengthNormal;
    }

    public void setLengthNormal(Double lengthNormal) {
        this.lengthNormal = lengthNormal;
    }

    public Double getLengthComplete() {
        return lengthComplete;
    }

    public void setLengthComplete(Double lengthComplete) {
        this.lengthComplete = lengthComplete;
    }

    public LocalDate getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(LocalDate releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public Integer getRatingCount() {
        return ratingCount;
    }

    public void setRatingCount(Integer ratingCount) {
        this.ratingCount = ratingCount;
    }

    public String getSynopsis() {
        return synopsis;
    }

    public void setSynopsis(String synopsis) {
        this.synopsis = synopsis;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setAll(GameRequestDto dto) {
        setName(dto.name());
        setLengthNormal(dto.lengthNormal());
        setLengthComplete(dto.lengthComplete());
        setReleaseDate(dto.releaseDate());
        setCoverUrl(dto.coverUrl());
        setRating(dto.rating());
        setRatingCount(dto.ratingCount());
        setSynopsis(dto.synopsis());
        setDescription(dto.description());
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Game game = (Game) o;
        return Objects.equals(id, game.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return name + " (ID: " + id + ")" +
                "\nLength (average): " + lengthNormal +
                "\nLength (100% completion): " + lengthComplete +
                "\nRelease Date: " + releaseDate.format(formatter) +
                "\nCover URL: " + coverUrl +
                "\nRating (" + ratingCount + " ratings): " + rating +
                "\nSynopsis: " + synopsis +
                "\nDescription: " + description;
    }
}
