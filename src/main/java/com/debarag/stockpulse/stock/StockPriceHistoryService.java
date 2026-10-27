
package com.debarag.stockpulse.stock;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.annotation.Propagation;
@Service
public class StockPriceHistoryService {

    private final StockRepository stockRepository;
    private final StockPriceHistoryRepository priceHistoryRepository;

    public StockPriceHistoryService(
            StockRepository stockRepository,
            StockPriceHistoryRepository priceHistoryRepository) {
        this.stockRepository = stockRepository;
        this.priceHistoryRepository = priceHistoryRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public StockPriceHistory recordPrice(Long stockId, BigDecimal price) {
        if (price == null || price.signum() < 0) {
            throw new IllegalArgumentException("Price must be zero or greater");
        }

        Stock stock = stockRepository.findById(stockId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Stock not found: " + stockId));

        StockPriceHistory history = new StockPriceHistory(stock, price);

        System.out.println(
                "Transaction active: " +
                        org.springframework.transaction.support.TransactionSynchronizationManager
                                .isActualTransactionActive()
        );

        System.out.println(
                "Transaction name: " +
                        TransactionSynchronizationManager.getCurrentTransactionName()
        );
        System.out.println(
                "Transaction read-only: " +
                        TransactionSynchronizationManager.isCurrentTransactionReadOnly()
        );

        StockPriceHistory saved = priceHistoryRepository.saveAndFlush(history);

        System.out.println(
                "PRICE SAVE CHECK: id=" + saved.getId()
                        + ", stockId=" + saved.getStock().getId()
                        + ", price=" + saved.getPrice()
        );

        return saved;
    }

    @Transactional(readOnly = true)
    public Optional<StockPriceHistory> getLatestPrice(Long stockId) {
        return priceHistoryRepository
                .findFirstByStockIdOrderByRecordedAtDesc(stockId);
    }

    @Transactional(readOnly = true)
    public List<StockPriceHistory> getPriceHistory(Long stockId) {
        return priceHistoryRepository
                .findByStockIdOrderByRecordedAtDesc(stockId);
    }
}
