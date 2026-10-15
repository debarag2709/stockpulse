
package com.debarag.stockpulse.user;

import com.debarag.stockpulse.config.JwtService;
import com.debarag.stockpulse.stock.Stock;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WatchlistController.class)
@Import(com.debarag.stockpulse.config.SecurityConfig.class)
class WatchlistControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WatchlistService watchlistService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private JwtService jwtService;

    @Test
    @WithMockUser(username = "test@example.com")
    void getWatchlist_shouldReturnEmptyList_whenWatchlistIsEmpty()
            throws Exception {

        User user = new User("test@example.com", "hashed-password");
        ReflectionTestUtils.setField(user, "id", 1L);

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));

        when(watchlistService.getWatchlist(any()))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/watchlist")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getWatchlist_shouldReturnStock_whenWatchlistContainsEntry()
            throws Exception {

        User user = new User("test@example.com", "hashed-password");
        ReflectionTestUtils.setField(user, "id", 1L);

        Stock stock = new Stock(
                "RELIANCE",
                "Reliance Industries Limited",
                "NSE"
        );
        ReflectionTestUtils.setField(stock, "id", 1L);

        Watchlist entry = new Watchlist(user, stock);

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));

        when(watchlistService.getWatchlist(any()))
                .thenReturn(List.of(entry));

        mockMvc.perform(get("/api/watchlist")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].symbol").value("RELIANCE"))
                .andExpect(jsonPath("$[0].companyName")
                        .value("Reliance Industries Limited"))
                .andExpect(jsonPath("$[0].exchange").value("NSE"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void addStock_shouldReturnCreated_whenStockIsAdded()
            throws Exception {

        User user = new User("test@example.com", "hashed-password");
        ReflectionTestUtils.setField(user, "id", 1L);

        Stock stock = new Stock(
                "RELIANCE",
                "Reliance Industries Limited",
                "NSE"
        );
        ReflectionTestUtils.setField(stock, "id", 1L);

        Watchlist entry = new Watchlist(user, stock);

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));

        when(watchlistService.addStock(1L, 1L))
                .thenReturn(entry);

        mockMvc.perform(post("/api/watchlist/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.symbol").value("RELIANCE"))
                .andExpect(jsonPath("$.stockId").value(1));
    }
}
