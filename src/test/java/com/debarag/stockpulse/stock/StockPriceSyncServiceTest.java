
package com.debarag.stockpulse.stock;

import com.debarag.stockpulse.alert.PriceAlertService;
import com.debarag.stockpulse.client.IndianApiClient;
import com.debarag.stockpulse.client.IndianApiStockResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StockPriceSyncServiceTest {

    @Mock
    private StockRepository stockRepository;

    @Mock
    private IndianApiClient indianApiClient;

    @Mock
    private StockPriceHistoryService priceHistoryService;

    @Mock
    private PriceAlertService priceAlertService;

    @InjectMocks
    private StockPriceSyncService syncService;

    private Stock stock;

    @BeforeEach
    void setUp() {
        stock = new Stock("TATASTEEL", "Tata Steel", "NSE");
    }

    @Test
    void shouldFetchAndRecordPriceForActiveStock() {
        when(stockRepository.findAll()).thenReturn(List.of(stock));

        IndianApiStockResponse response = new IndianApiStockResponse(
                "Tata Steel",
                "TATASTEEL",
                "500470",
                Map.of("NSE", "175.74", "BSE", "175.74"),
                "-0.37",
                "224.40",
                "160.00"
        );

        when(indianApiClient.getStockData("TATASTEEL"))
                .thenReturn(response);

        int recordedCount = syncService.syncActiveStocks();

        assertEquals(1, recordedCount);

        verify(indianApiClient).getStockData("TATASTEEL");

        verify(priceHistoryService)
                .recordPrice(stock.getId(), new BigDecimal("175.74"));
    }

    @Test
    void shouldSkipInactiveStocks() {
        stock.setActive(false);

        when(stockRepository.findAll()).thenReturn(List.of(stock));

        int recordedCount = syncService.syncActiveStocks();

        assertEquals(0, recordedCount);

        verify(indianApiClient, never()).getStockData(any());

        verify(priceHistoryService, never())
                .recordPrice(any(), any());
    }

    @Test
    void shouldSkipStockWhenExchangePriceIsMissing() {
        when(stockRepository.findAll()).thenReturn(List.of(stock));

        IndianApiStockResponse response = new IndianApiStockResponse(
                "Tata Steel",
                "TATASTEEL",
                "500470",
                Map.of("BSE", "175.74"),
                "-0.37",
                "224.40",
                "160.00"
        );

        when(indianApiClient.getStockData("TATASTEEL"))
                .thenReturn(response);

        int recordedCount = syncService.syncActiveStocks();

        assertEquals(0, recordedCount);

        verify(indianApiClient).getStockData("TATASTEEL");

        verify(priceHistoryService, never())
                .recordPrice(any(), any());
    }

    @Test
    void shouldContinueWhenOneStockFails() {
        Stock secondStock = new Stock("INFY", "Infosys", "NSE");

        when(stockRepository.findAll())
                .thenReturn(List.of(stock, secondStock));

        when(indianApiClient.getStockData("TATASTEEL"))
                .thenThrow(new RuntimeException("Provider unavailable"));

        IndianApiStockResponse response = new IndianApiStockResponse(
                "Infosys",
                "INFY",
                "500209",
                Map.of("NSE", "1500.00"),
                "0.5",
                "1800.00",
                "1300.00"
        );

        when(indianApiClient.getStockData("INFY"))
                .thenReturn(response);

        int recordedCount = syncService.syncActiveStocks();

        assertEquals(1, recordedCount);

        verify(indianApiClient).getStockData("TATASTEEL");
        verify(indianApiClient).getStockData("INFY");

        verify(priceHistoryService)
                .recordPrice(secondStock.getId(), new BigDecimal("1500.00"));
    }
}
