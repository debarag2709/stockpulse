
package com.debarag.stockpulse.stock;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class StockPriceHistoryService {

    private final StockPriceHistoryRepository priceHistoryRepository;
    private final StockRepository stockRepository;

    public StockPriceHistoryService(
            StockPriceHistoryRepository priceHistoryRepository,
            StockRepository stockRepository) {
        this.priceHistoryRepository = priceHistoryRepository;
        this.stockRepository = stockRepository;
    }

    @Transactional
    public StockPriceHistory recordPrice(Long stockId, BigDecimal price) {
        if (price == null || price.signum() < 0) {
            throw new IllegalArgumentException("Price must be zero or greater");
        }

        var stock = stockRepository.findById(stockId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Stock not found: " + stockId));

        StockPriceHistory history = new StockPriceHistory(stock, price);
        return priceHistoryRepository.saveAndFlush(history);
    }

    @Transactional(readOnly = true)
    public Optional<StockPriceHistory> getLatestPrice(Long stockId) {
        return priceHistoryRepository
                .findFirstByStockIdOrderByRecordedAtDesc(stockId);
    }

    @Transactional(readOnly = true)
    public List<StockPriceHistory> getPriceHistory(Long stockId) {
        if (!stockRepository.existsById(stockId)) {
            throw new IllegalArgumentException("Stock not found: " + stockId);
        }

        return priceHistoryRepository
                .findByStockIdOrderByRecordedAtDesc(stockId);
    }
}
