package dev.abdulrahman.excelsior.business.game;

import dev.abdulrahman.excelsior.business.game.dto.GameRequestDto;
import dev.abdulrahman.excelsior.business.game.dto.GameResponseDto;
import dev.abdulrahman.excelsior.business.game.dto.GameStatsDto;
import dev.abdulrahman.excelsior.business.game.dto.GameTierPositionPatchDto;
import dev.abdulrahman.excelsior.business.user.User;
import dev.abdulrahman.excelsior.integration.ClientService;
import dev.abdulrahman.excelsior.mapper.DtoMapper;
import dev.abdulrahman.excelsior.persistence.GameRepository;
import dev.abdulrahman.excelsior.persistence.UserRepository;
import dev.abdulrahman.excelsior.presentation.exception.GameAlreadyExistsException;
import dev.abdulrahman.excelsior.presentation.exception.GameNotFoundException;
import dev.abdulrahman.excelsior.presentation.exception.UserNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

@Service
public class GameService {
    private final GameRepository gameRepository;
    private final ClientService clientService;
    private final DtoMapper mapper;
    private final UserRepository userRepository;

    public GameService(GameRepository gameRepository, ClientService clientService,
                       DtoMapper mapper, UserRepository userRepository) {
        this.gameRepository = gameRepository;
        this.clientService = clientService;
        this.mapper = mapper;
        this.userRepository = userRepository;
    }

    public List<GameResponseDto> searchGame(String query, Integer limit, Integer offset) {
        return clientService.searchGames(query, limit, offset, getCurrentAuthUser());
    }

    public void addGame(GameRequestDto dto) {
        if (gameRepository.existsByUserAndIgdbId(getCurrentAuthUser(), dto.igdbId())) {
            throw new GameAlreadyExistsException("Game with IGDB ID '" + dto.igdbId() +
                    "' (" + dto.name() + ") already exists");
        }
        gameRepository.save(mapper.toGameEntity(dto, getCurrentAuthUser()));
    }

    public void addAllGames(Collection<GameRequestDto> dtos) {
        dtos.forEach(this::addGame);
    }

    public GameResponseDto getGameById(Long id) {
        return mapper.toGameResponseDto(findGameByUserAndId(id));
    }

    public void updateGame(Long id, GameRequestDto dto) {
        Game game = findGameByUserAndId(id);
        game.setAll(dto);
        gameRepository.save(game);
    }

    public void updateTier(Long id, GameTier tier) {
        updateField(id, Game::setTierValue, tier);
    }

    public void updateStatus(Long id, GameStatus status) {
        updateField(id, Game::setStatus, status);
    }

    public void updateTierPosition(GameTierPositionPatchDto dto) {
        Game game = findGameByUserAndId(dto.id());
        game.setTierValue(dto.tier());
        game.setTierPosition(dto.position());
        gameRepository.save(game);
    }

    public void updateTierPositionForAll(Collection<GameTierPositionPatchDto> dtos) {
        dtos.forEach(this::updateTierPosition);
    }

    public void deleteGame(Long id) {
        gameRepository.delete(findGameByUserAndId(id));
    }

    public Page<GameResponseDto> findGamesDynamically(
            Double minRating, Double maxRating, String nameContains, String genreContains,
            Double minLengthNormal, Double maxLengthNormal, Double minLengthComplete, Double maxLengthComplete, 
            LocalDate minDate, LocalDate maxDate, Integer minYear, Integer maxYear, 
            GameTier tier, GameStatus status, String platformContains, Pageable pageable
    ) {
        return gameRepository.findDynamically(
                getCurrentAuthUser(), minRating, maxRating, nameContains, genreContains,
                minLengthNormal, maxLengthNormal, minLengthComplete, maxLengthComplete, 
                minDate, maxDate, minYear, maxYear, tier, status, platformContains, pageable
        ).map(mapper::toGameResponseDto);
    }
    
    public GameStatsDto getStats() {
        List<Game> allGames = gameRepository.findAllByUser(getCurrentAuthUser());
        long total = allGames.size();
        
        Map<GameStatus, Long> byStatus = allGames.stream()
                .collect(Collectors.groupingBy(Game::getStatus, Collectors.counting()));
                
        Map<GameTier, Long> byTier = allGames.stream()
                .collect(Collectors.groupingBy(Game::getTierValue, Collectors.counting()));
                
        return new GameStatsDto(total, byStatus, byTier);
    }

    private GameNotFoundException idNotFound(Long id) {
        return new GameNotFoundException("Could not find a game with ID '" + id + "'");
    }

    private Game findGameByUserAndId(Long id) {
        return gameRepository.findByUserAndId(getCurrentAuthUser(), id)
                .orElseThrow(() -> idNotFound(id));
    }

    private <T> void updateField(Long id, BiConsumer<Game, T> mutator, T field) {
        Game game = findGameByUserAndId(id);
        mutator.accept(game, field);
        gameRepository.save(game);
    }

    private User getCurrentAuthUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return null;
        }

        String username = auth.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(UserNotFoundException::new);
    }
}
