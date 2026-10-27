
package com.debarag.stockpulse.stock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StockServiceTest {

    @Mock
    private StockRepository stockRepository;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    private StockService stockService;

    @BeforeEach
    void setUp() {
        stockService = new StockService(stockRepository, eventPublisher);
    }

    @Test
    void createStock_shouldSaveStock_whenSymbolDoesNotExist() {
        when(stockRepository.existsBySymbol("INFY"))
                .thenReturn(false);

        when(stockRepository.save(any(Stock.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Stock result = stockService.createStock(
                "infy",
                "Infosys",
                "nse"
        );

        assertEquals("INFY", result.getSymbol());
        assertEquals("Infosys", result.getCompanyName());
        assertEquals("NSE", result.getExchange());

        verify(stockRepository).save(any(Stock.class));
    }

    @Test
    void createStock_shouldThrowException_whenSymbolAlreadyExists() {
        when(stockRepository.existsBySymbol("INFY"))
                .thenReturn(true);

        assertThrows(
                DuplicateStockException.class,
                () -> stockService.createStock(
                        "infy",
                        "Infosys",
                        "nse"
                )
        );

        verify(stockRepository, never()).save(any(Stock.class));
    }


    @Test
    void updateStock_shouldUpdateFields_whenStockExists() {
        Stock stock = new Stock("INFY", "Infosys", "NSE");

        when(stockRepository.findById(1L))
                .thenReturn(java.util.Optional.of(stock));

        when(stockRepository.save(any(Stock.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Stock result = stockService.updateStock(
                1L,
                "Infosys Limited",
                "bse",
                false
        );

        assertEquals("INFY", result.getSymbol());
        assertEquals("Infosys Limited", result.getCompanyName());
        assertEquals("BSE", result.getExchange());
        assertFalse(result.isActive());

        verify(stockRepository).save(stock);
    }


    @Test
    void updateStock_shouldThrowException_whenStockDoesNotExist() {
        when(stockRepository.findById(999999L))
                .thenReturn(java.util.Optional.empty());

        assertThrows(
                StockNotFoundException.class,
                () -> stockService.updateStock(
                        999999L,
                        "Unknown Company",
                        "NSE",
                        true
                )
        );

        verify(stockRepository, never()).save(any(Stock.class));
    }


}
