
package com.debarag.stockpulse.user;

import com.debarag.stockpulse.stock.Stock;
import com.debarag.stockpulse.stock.StockRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WatchlistServiceTest {

    @Mock
    private WatchlistRepository watchlistRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private StockRepository stockRepository;

    @InjectMocks
    private WatchlistService watchlistService;

    private User user;
    private Stock stock;

    @BeforeEach
    void setUp() {
        user = new User("test@example.com", "hashed-password");
        stock = new Stock(
                "RELIANCE",
                "Reliance Industries Limited",
                "NSE"
        );
    }

    @Test
    void addStock_shouldSaveEntry_whenStockIsNotAlreadyInWatchlist() {
        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));
        when(stockRepository.findById(1L))
                .thenReturn(Optional.of(stock));
        when(watchlistRepository.existsByUserIdAndStockId(1L, 1L))
                .thenReturn(false);

        Watchlist savedEntry = new Watchlist(user, stock);

        when(watchlistRepository.save(any(Watchlist.class)))
                .thenReturn(savedEntry);

        Watchlist result = watchlistService.addStock(1L, 1L);

        assertNotNull(result);
        assertEquals("RELIANCE", result.getStock().getSymbol());

        verify(watchlistRepository).save(any(Watchlist.class));
    }

    @Test
    void addStock_shouldThrowException_whenStockAlreadyInWatchlist() {
        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(stockRepository.findById(1L))
                .thenReturn(Optional.of(stock));

        when(watchlistRepository.existsByUserIdAndStockId(1L, 1L))
                .thenReturn(true);

        DuplicateWatchlistException exception = assertThrows(
                DuplicateWatchlistException.class,
                () -> watchlistService.addStock(1L, 1L)
        );

        assertEquals(
                "Stock is already in the watchlist",
                exception.getMessage()
        );

        verify(watchlistRepository, never()).save(any(Watchlist.class));
    }

    @Test
    void removeStock_shouldDeleteEntry_whenStockIsInWatchlist() {
        when(userRepository.existsById(1L)).thenReturn(true);

        when(watchlistRepository.existsByUserIdAndStockId(1L, 1L))
                .thenReturn(true);

        watchlistService.removeStock(1L, 1L);

        verify(watchlistRepository)
                .deleteByUserIdAndStockId(1L, 1L);
    }

    @Test
    void removeStock_shouldThrowException_whenStockIsNotInWatchlist() {
        when(userRepository.existsById(1L)).thenReturn(true);

        when(watchlistRepository.existsByUserIdAndStockId(1L, 1L))
                .thenReturn(false);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> watchlistService.removeStock(1L, 1L)
        );

        assertEquals(
                "Stock is not in the watchlist",
                exception.getMessage()
        );

        verify(watchlistRepository, never())
                .deleteByUserIdAndStockId(1L, 1L);
    }

    @Test
    void removeStock_shouldThrowException_whenUserDoesNotExist() {
        when(userRepository.existsById(1L)).thenReturn(false);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> watchlistService.removeStock(1L, 1L)
        );

        assertEquals("User not found", exception.getMessage());

        verify(watchlistRepository, never())
                .deleteByUserIdAndStockId(1L, 1L);
    }
}
