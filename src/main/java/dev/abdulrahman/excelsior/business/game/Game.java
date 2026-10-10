package dev.abdulrahman.excelsior.business.game;

import dev.abdulrahman.excelsior.business.game.dto.GameRequestDto;
import dev.abdulrahman.excelsior.business.user.User;
import jakarta.persistence.*;
import org.hibernate.annotations.Formula;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

@Entity
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long igdbId;

    private String name;
    private Double lengthNormal;
    private Double lengthComplete;
    private LocalDate releaseDate;
    private String coverUrl;
    private Double rating;

    private Integer ratingCount;
    @Column(length = 5000) private String synopsis;
    @Column(length = 5000) private String description;
    private String primaryEngine;
    @ElementCollection private List<String> genres;
    @ElementCollection private List<String> companies;

    @Enumerated(EnumType.STRING)
    @Column(name = "tier")
    private GameTier tierValue;
    
    @Enumerated(EnumType.STRING) private GameStatus status;
    @ElementCollection private List<String> screenshotUrls;
    @ElementCollection private List<String> platforms;

    @Formula("(CASE tier WHEN 'S' THEN 6 WHEN 'A' THEN 5 WHEN 'B' THEN 4 WHEN 'C' THEN 3 WHEN 'D' THEN 2 ELSE 1 END)")
    private Integer tier;
    private Integer tierPosition;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    protected Game() {}

    public Game(Long igdbId,
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
                GameTier tierValue,
                GameStatus status,
                List<String> screenshotUrls,
                List<String> platforms,
                User user
    ) {
        setIgdbId(igdbId);
        setName(name);
        setLengthNormal(lengthNormal);
        setLengthComplete(lengthComplete);
        setReleaseDate(releaseDate);
        setCoverUrl(coverUrl);
        setRating(rating);
        setRatingCount(ratingCount);
        setSynopsis(synopsis);
        setDescription(description);
        setPrimaryEngine(primaryEngine);
        setGenres(genres);
        setCompanies(companies);
        setTierValue(tierValue);
        setStatus(status);
        setScreenshotUrls(screenshotUrls);
        setPlatforms(platforms);
        setTierPosition(0);
        setUser(user);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getIgdbId() {
        return igdbId;
    }

    public void setIgdbId(Long igdbId) {
        this.igdbId = igdbId;
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

    public String getPrimaryEngine() {
        return primaryEngine;
    }

    public void setPrimaryEngine(String primaryEngine) {
        this.primaryEngine = primaryEngine;
    }

    public List<String> getGenres() {
        return genres;
    }

    public void setGenres(List<String> genres) {
        this.genres = genres;
    }

    public List<String> getCompanies() {
        return companies;
    }

    public void setCompanies(List<String> companies) {
        this.companies = companies;
    }

    public GameTier getTierValue() {
        return tierValue;
    }

    public void setTierValue(GameTier tierValue) {
        this.tierValue = tierValue;
    }

    public GameStatus getStatus() {
        return status;
    }

    public void setStatus(GameStatus status) {
        this.status = status;
    }

    public List<String> getScreenshotUrls() {
        return screenshotUrls;
    }

    public void setScreenshotUrls(List<String> screenshotUrls) {
        this.screenshotUrls = screenshotUrls;
    }

    public List<String> getPlatforms() {
        return platforms;
    }

    public void setPlatforms(List<String> platforms) {
        this.platforms = platforms;
    }

    public Integer getTier() {
        return tier;
    }

    public Integer getTierPosition() {
        return tierPosition;
    }

    public void setTierPosition(Integer tierPosition) {
        this.tierPosition = tierPosition;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setAll(GameRequestDto dto) {
        setIgdbId(dto.igdbId());
        setName(dto.name());
        setLengthNormal(dto.lengthNormal());
        setLengthComplete(dto.lengthComplete());
        setReleaseDate(dto.releaseDate());
        setCoverUrl(dto.coverUrl());
        setRating(dto.rating());
        setRatingCount(dto.ratingCount());
        setSynopsis(dto.synopsis());
        setDescription(dto.description());
        setPrimaryEngine(dto.primaryEngine());
        setGenres(dto.genres());
        setCompanies(dto.companies());
        setTierValue(dto.tier());
        setStatus(dto.status());
        setScreenshotUrls(dto.screenshotUrls());
        setPlatforms(dto.platforms());
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
}
