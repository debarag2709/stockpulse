
package com.debarag.stockpulse.stock;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stocks/prices")
public class StockPriceSyncController {

    private final StockPriceSyncService syncService;

    public StockPriceSyncController(StockPriceSyncService syncService) {
        this.syncService = syncService;
    }

    @PostMapping("/sync")
    public SyncResponse syncPrices() {
        int recordedCount = syncService.syncActiveStocks();
        return new SyncResponse(
                "Stock price synchronization completed",
                recordedCount
        );
    }

    public record SyncResponse(String message, int recordedCount) {
    }
}
