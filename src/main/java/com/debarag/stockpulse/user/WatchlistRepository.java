
package com.debarag.stockpulse.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;
import java.util.Optional;

public interface WatchlistRepository
        extends JpaRepository<Watchlist, Long> {

    @EntityGraph(attributePaths = {"stock"})
    List<Watchlist> findByUserId(Long userId);

    Optional<Watchlist> findByUserIdAndStockId(
            Long userId,
            Long stockId
    );

    boolean existsByUserIdAndStockId(
            Long userId,
            Long stockId
    );

    void deleteByUserIdAndStockId(
            Long userId,
            Long stockId
    );
}
