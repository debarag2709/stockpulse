
package com.debarag.stockpulse.stock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StockPriceHistoryServiceTest {

    @Mock
    private StockPriceHistoryRepository priceHistoryRepository;

    @Mock
    private StockRepository stockRepository;

    @InjectMocks
    private StockPriceHistoryService service;

    private Stock stock;

    @BeforeEach
    void setUp() {
        stock = new Stock("TATASTEEL", "Tata Steel", "NSE");
    }

    @Test
    void recordPriceShouldSaveHistory() {
        when(stockRepository.findById(1L))
                .thenReturn(Optional.of(stock));

        when(priceHistoryRepository.saveAndFlush(any(StockPriceHistory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        StockPriceHistory result =
                service.recordPrice(1L, new BigDecimal("175.74"));

        assertEquals(new BigDecimal("175.74"), result.getPrice());
        assertEquals(stock, result.getStock());
        verify(priceHistoryRepository).saveAndFlush(any(StockPriceHistory.class));
    }

    @Test
    void recordPriceShouldRejectNegativePrice() {
        assertThrows(IllegalArgumentException.class,
                () -> service.recordPrice(1L, new BigDecimal("-1.00")));
    }

    @Test
    void recordPriceShouldRejectNullPrice() {
        assertThrows(IllegalArgumentException.class,
                () -> service.recordPrice(1L, null));
    }

    @Test
    void recordPriceShouldRejectUnknownStock() {
        when(stockRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> service.recordPrice(99L, new BigDecimal("100.00")));
    }

    @Test
    void getLatestPriceShouldReturnRepositoryResult() {
        StockPriceHistory history =
                new StockPriceHistory(stock, new BigDecimal("175.74"));

        when(priceHistoryRepository
                .findFirstByStockIdOrderByRecordedAtDesc(1L))
                .thenReturn(Optional.of(history));

        Optional<StockPriceHistory> result = service.getLatestPrice(1L);

        assertTrue(result.isPresent());
        assertEquals(new BigDecimal("175.74"), result.get().getPrice());
    }
}
