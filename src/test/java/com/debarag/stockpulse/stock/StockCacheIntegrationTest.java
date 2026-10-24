package com.debarag.stockpulse.stock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class StockCacheIntegrationTest {

    @Autowired
    private StockService stockService;

    @Autowired
    private CacheManager cacheManager;

    private Cache stockCache;

    @BeforeEach
    void setUp() {
        stockCache = cacheManager.getCache("stocks");

        assertNotNull(stockCache, "Stocks cache should exist");
        stockCache.clear();
    }

    @Test
    void getStockById_shouldCacheStock() {
        // Load an existing stock from the database.
        Stock stock = stockService.getStockById(1L);

        assertNotNull(stock);

        // Verify that the stock was stored in the cache.
        Cache.ValueWrapper cachedValue = stockCache.get(1L);

        assertNotNull(cachedValue, "Stock should be cached");
        assertInstanceOf(Stock.class, cachedValue.get());
        assertEquals(stock.getId(), ((Stock) cachedValue.get()).getId());
    }

    @Test
    void updateStock_shouldEvictCachedStock() {
        // Ensure stock ID 1 exists and is cached.
        Stock existingStock = stockService.getStockById(1L);
        assertNotNull(stockCache.get(1L));

        // Update the stock.
        stockService.updateStock(
                1L,
                existingStock.getCompanyName(),
                existingStock.getExchange(),
                existingStock.isActive()
        );

        // Verify the old cache entry was evicted.
        assertNull(
                stockCache.get(1L),
                "Cached stock should be evicted after an update"
        );
    }
}