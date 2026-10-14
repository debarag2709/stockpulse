
package com.debarag.stockpulse.stock;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


import java.util.List;
import java.util.Locale;

@Service
public class StockService {

    private final StockRepository stockRepository;

    public StockService(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    @Transactional
    public Stock createStock(String symbol, String companyName, String exchange) {
        String normalizedSymbol = symbol.trim().toUpperCase(Locale.ROOT);

        if (stockRepository.existsBySymbol(normalizedSymbol)) {
            throw new DuplicateStockException(normalizedSymbol);
        }

        Stock stock = new Stock(
                normalizedSymbol,
                companyName.trim(),
                exchange.trim().toUpperCase(Locale.ROOT)
        );

        return stockRepository.save(stock);
    }

    @Transactional(readOnly = true)
    public List<Stock> getAllStocks() {
        return stockRepository.findAll();
    }


    public Page<Stock> getAllStocks(Pageable pageable) {
        return stockRepository.findAll(pageable);
    }



    public Stock getStockById(Long id) {
        return stockRepository.findById(id)
                .orElseThrow(() -> new StockNotFoundException(id));
    }


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
