
package com.debarag.stockpulse.stock;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StockPriceHistoryRepository
        extends JpaRepository<StockPriceHistory, Long> {

    List<StockPriceHistory> findByStockIdOrderByRecordedAtDesc(Long stockId);

    Optional<StockPriceHistory> findFirstByStockIdOrderByRecordedAtDesc(Long stockId);
}
