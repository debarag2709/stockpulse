
package com.debarag.stockpulse.stock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class StockCreatedPriceSyncListener {

    private static final Logger log =
            LoggerFactory.getLogger(StockCreatedPriceSyncListener.class);

    private final StockPriceSyncService stockPriceSyncService;

    public StockCreatedPriceSyncListener(
            StockPriceSyncService stockPriceSyncService) {
        this.stockPriceSyncService = stockPriceSyncService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onStockCreated(StockCreatedEvent event) {
        log.info(
                "Starting initial price sync for stock ID {}",
                event.stockId()
        );

        try {
            boolean recorded =
                    stockPriceSyncService.syncStockById(event.stockId());

            if (!recorded) {
                log.warn(
                        "Initial price sync did not record a price for stock ID {}. "
                                + "The daily scheduler can retry.",
                        event.stockId()
                );
            }
        } catch (RuntimeException exception) {
            // Stock creation has already committed; don't undo it.
            log.error(
                    "Initial price sync failed for stock ID {}",
                    event.stockId(),
                    exception
            );
        }
    }
}
