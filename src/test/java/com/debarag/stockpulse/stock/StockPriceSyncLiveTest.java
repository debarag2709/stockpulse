
package com.debarag.stockpulse.stock;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.EnabledIf;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:postgresql://localhost:5433/stockpulse",
        "spring.datasource.username=stockpulse"
})
@EnabledIf(
        expression = "${stockpulse.live-tests.enabled:false}",
        loadContext = true
)
class StockPriceSyncLiveTest {

    @Autowired
    private StockPriceSyncService syncService;

    @Test
    void syncActiveStocksAndPrintResult() {
        int recordedCount = syncService.syncActiveStocks();

        System.out.println(
                "Live sync completed. Prices recorded: " + recordedCount);

        assertTrue(recordedCount >= 0);
    }
}
