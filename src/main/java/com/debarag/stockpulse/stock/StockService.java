
package com.debarag.stockpulse.stock;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Locale;

@Service
public class StockService {

    private final StockRepository stockRepository;
    private final ApplicationEventPublisher eventPublisher;

    public StockService(
            StockRepository stockRepository,
            ApplicationEventPublisher eventPublisher) {
        this.stockRepository = stockRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public Stock createStock(
            String symbol,
            String companyName,
            String exchange) {

        String normalizedSymbol =
                symbol.trim().toUpperCase(Locale.ROOT);

        if (stockRepository.existsBySymbol(normalizedSymbol)) {
            throw new DuplicateStockException(normalizedSymbol);
        }

        Stock stock = new Stock(
                normalizedSymbol,
                companyName.trim(),
                exchange.trim().toUpperCase(Locale.ROOT)
        );

        Stock savedStock = stockRepository.save(stock);

        // The listener will run only after this transaction commits.
        eventPublisher.publishEvent(
                new StockCreatedEvent(savedStock.getId())
        );

        return savedStock;
    }

    @Transactional(readOnly = true)
    public List<Stock> getAllStocks() {
        return stockRepository.findAll();
    }

    public Page<Stock> getAllStocks(Pageable pageable) {
        return stockRepository.findAll(pageable);
    }

    @Cacheable(value = "stocks", key = "#id")
    public Stock getStockById(Long id) {
        return stockRepository.findById(id)
                .orElseThrow(() -> new StockNotFoundException(id));
    }

    @CacheEvict(value = "stocks", key = "#id")
    @Transactional
    public Stock updateStock(
            Long id,
            String companyName,
            String exchange,
            boolean active) {

        Stock stock = stockRepository.findById(id)
                .orElseThrow(() -> new StockNotFoundException(id));

        stock.setCompanyName(companyName.trim());
        stock.setExchange(exchange.trim().toUpperCase(Locale.ROOT));
        stock.setActive(active);

        return stockRepository.save(stock);
    }
}
