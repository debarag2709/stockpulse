
package com.debarag.stockpulse.stock;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StockPriceSchedulerTest {

    @Mock
    private StockPriceSyncService syncService;

    @InjectMocks
    private StockPriceScheduler scheduler;

    @Test
    void shouldRunSynchronizationAndReturnRecordedCount() {
        when(syncService.syncActiveStocks()).thenReturn(3);

        scheduler.syncStockPrices();

        verify(syncService).syncActiveStocks();
    }

    @Test
    void shouldHandleSynchronizationFailure() {
        when(syncService.syncActiveStocks())
                .thenThrow(new RuntimeException("Provider unavailable"));

        // Should not propagate the exception.
        scheduler.syncStockPrices();

        verify(syncService).syncActiveStocks();
    }
}
