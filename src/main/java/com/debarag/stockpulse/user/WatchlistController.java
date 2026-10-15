
package com.debarag.stockpulse.user;

import com.debarag.stockpulse.stock.Stock;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/watchlist")
public class WatchlistController {

    private final WatchlistService watchlistService;
    private final UserRepository userRepository;

    public WatchlistController(
            WatchlistService watchlistService,
            UserRepository userRepository
    ) {
        this.watchlistService = watchlistService;
        this.userRepository = userRepository;
    }

    @PostMapping("/{stockId}")
    @ResponseStatus(HttpStatus.CREATED)
    public WatchlistResponse addStock(
            @PathVariable Long stockId,
            Authentication authentication
    ) {
        User user = getAuthenticatedUser(authentication);

        Watchlist entry = watchlistService.addStock(
                user.getId(),
                stockId
        );

        return toResponse(entry);
    }

    @GetMapping
    public List<WatchlistResponse> getWatchlist(
            Authentication authentication
    ) {
        User user = getAuthenticatedUser(authentication);

        return watchlistService.getWatchlist(user.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @DeleteMapping("/{stockId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeStock(
            @PathVariable Long stockId,
            Authentication authentication
    ) {
        User user = getAuthenticatedUser(authentication);
        watchlistService.removeStock(user.getId(), stockId);
    }

    private User getAuthenticatedUser(Authentication authentication) {
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"));
    }

    private WatchlistResponse toResponse(Watchlist entry) {
        Stock stock = entry.getStock();

        return new WatchlistResponse(
                entry.getId(),
                stock.getId(),
                stock.getSymbol(),
                stock.getCompanyName(),
                stock.getExchange(),
                entry.getCreatedAt()
        );
    }

    public record WatchlistResponse(
            Long id,
            Long stockId,
            String symbol,
            String companyName,
            String exchange,
            OffsetDateTime createdAt
    ) {}
}
