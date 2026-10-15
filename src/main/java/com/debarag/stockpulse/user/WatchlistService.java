
package com.debarag.stockpulse.user;

import com.debarag.stockpulse.stock.Stock;
import com.debarag.stockpulse.stock.StockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class WatchlistService {

    private final WatchlistRepository watchlistRepository;
    private final UserRepository userRepository;
    private final StockRepository stockRepository;

    public WatchlistService(
            WatchlistRepository watchlistRepository,
            UserRepository userRepository,
            StockRepository stockRepository
    ) {
        this.watchlistRepository = watchlistRepository;
        this.userRepository = userRepository;
        this.stockRepository = stockRepository;
    }

    @Transactional
    public Watchlist addStock(Long userId, Long stockId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"));

        Stock stock = stockRepository.findById(stockId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Stock not found"));

        if (watchlistRepository.existsByUserIdAndStockId(userId, stockId)) {
            throw new DuplicateWatchlistException(
                    "Stock is already in the watchlist");
        }

        return watchlistRepository.save(new Watchlist(user, stock));
    }

    @Transactional(readOnly = true)
    public List<Watchlist> getWatchlist(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("User not found");
        }

        return watchlistRepository.findByUserId(userId);
    }

    @Transactional
    public void removeStock(Long userId, Long stockId) {
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("User not found");
        }

        if (!watchlistRepository.existsByUserIdAndStockId(userId, stockId)) {
            throw new IllegalArgumentException(
                    "Stock is not in the watchlist");
        }

        watchlistRepository.deleteByUserIdAndStockId(userId, stockId);
    }
}
