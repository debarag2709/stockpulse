
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
        log.info("StockPriceScheduler bean created");
    }

    /**
     * Runs once every day at 9 PM IST.
     */
    @Scheduled(
            cron = "0 0 21 * * *",
            zone = "Asia/Kolkata"
    )
    public void syncStockPrices() {
        log.info("Starting daily stock-price synchronization");

        try {
            int recordedCount = syncService.syncActiveStocks();

            log.info(
                    "Daily stock-price synchronization completed. Prices recorded: {}",
                    recordedCount
            );
        } catch (RuntimeException exception) {
            log.error(
                    "Daily stock-price synchronization failed",
                    exception
            );
        }
    }
}
