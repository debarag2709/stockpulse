
package com.debarag.stockpulse.stock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class StockPriceScheduler {

    private static final Logger log =
            LoggerFactory.getLogger(StockPriceScheduler.class);

    private final StockPriceSyncService syncService;

    public StockPriceScheduler(StockPriceSyncService syncService) {
        this.syncService = syncService;
    }

    /**
     * Runs at 10 AM, 12 PM and 2 PM IST on weekdays.
     * This conservative schedule helps preserve the free API quota.
     */
    @Scheduled(cron = "0 0 10,12,14 * * MON-FRI", zone = "Asia/Kolkata")
    public void syncStockPrices() {
        log.info("Starting scheduled stock-price synchronization");

        try {
            int recordedCount = syncService.syncActiveStocks();
            log.info(
                    "Scheduled stock-price synchronization completed. Prices recorded: {}",
                    recordedCount
            );
        } catch (RuntimeException exception) {
            log.error(
                    "Scheduled stock-price synchronization failed",
                    exception
            );
        }
    }
}
