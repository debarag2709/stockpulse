
package com.debarag.stockpulse.stock;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class StockIntegrationTest {

    @Autowired
    private StockService stockService;

    @Autowired
    private StockRepository stockRepository;

    @Test
    void createStock_shouldPersistStockInDatabase() {
        Stock savedStock = stockService.createStock(
                "TESTINFY",
                "Test Infosys",
                "NSE"
        );

        assertNotNull(savedStock.getId());

        Stock retrievedStock = stockRepository
                .findBySymbol("TESTINFY")
                .orElseThrow();

        assertEquals("TESTINFY", retrievedStock.getSymbol());
        assertEquals("Test Infosys", retrievedStock.getCompanyName());
        assertEquals("NSE", retrievedStock.getExchange());
    }

    @Test
    void createStock_shouldRejectDuplicateSymbol() {
        stockService.createStock(
                "TESTTCS",
                "Test TCS",
                "NSE"
        );

        assertThrows(
                DuplicateStockException.class,
                () -> stockService.createStock(
                        "TESTTCS",
                        "Another TCS",
                        "BSE"
                )
        );
    }
}
