package com.debarag.stockpulse.stock;

import com.debarag.stockpulse.common.GlobalExceptionHandler;
import com.debarag.stockpulse.config.JwtService;
import com.debarag.stockpulse.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StockController.class)
@Import({
        GlobalExceptionHandler.class,
        com.debarag.stockpulse.config.SecurityConfig.class
})
class StockControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StockService stockService;

    @MockitoBean
    private StockPriceSyncService stockPriceSyncService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    @WithMockUser(username = "test@example.com")
    void createStock_shouldReturn201_whenRequestIsValid() throws Exception {
        Stock stock = new Stock("INFY", "Infosys", "NSE");

        when(stockService.createStock("INFY", "Infosys", "NSE"))
                .thenReturn(stock);

        mockMvc.perform(post("/api/stocks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "symbol": "INFY",
                                  "companyName": "Infosys",
                                  "exchange": "NSE"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.symbol").value("INFY"))
                .andExpect(jsonPath("$.companyName").value("Infosys"))
                .andExpect(jsonPath("$.exchange").value("NSE"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void createStock_shouldReturn400_whenSymbolIsBlank() throws Exception {
        mockMvc.perform(post("/api/stocks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "symbol": "",
                                  "companyName": "Infosys",
                                  "exchange": "NSE"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getAllStocks_shouldReturn200() throws Exception {
        Stock stock = new Stock("INFY", "Infosys", "NSE");

        org.springframework.data.domain.Page<Stock> stockPage =
                new org.springframework.data.domain.PageImpl<>(
                        List.of(stock)
                );

        when(stockService.getAllStocks(any(
                org.springframework.data.domain.Pageable.class
        ))).thenReturn(stockPage);

        mockMvc.perform(get("/api/stocks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].symbol").value("INFY"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getStockById_shouldReturn200_whenStockExists() throws Exception {
        Stock stock = new Stock("RELIANCE", "Reliance Industries", "NSE");

        when(stockService.getStockById(1L)).thenReturn(stock);

        mockMvc.perform(get("/api/stocks/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.symbol").value("RELIANCE"))
                .andExpect(jsonPath("$.companyName").value("Reliance Industries"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getStockById_shouldReturn404_whenStockDoesNotExist() throws Exception {
        when(stockService.getStockById(999999L))
                .thenThrow(new StockNotFoundException(999999L));

        mockMvc.perform(get("/api/stocks/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Stock not found"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void updateStock_shouldReturn200_whenRequestIsValid() throws Exception {
        Stock stock = new Stock(
                "RELIANCE",
                "Reliance Industries Limited",
                "NSE"
        );

        when(stockService.updateStock(
                1L,
                "Reliance Industries Limited",
                "NSE",
                true
        )).thenReturn(stock);

        mockMvc.perform(put("/api/stocks/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "companyName": "Reliance Industries Limited",
                                  "exchange": "NSE",
                                  "active": true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.symbol").value("RELIANCE"))
                .andExpect(jsonPath("$.companyName")
                        .value("Reliance Industries Limited"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void updateStock_shouldReturn400_whenCompanyNameIsBlank() throws Exception {
        mockMvc.perform(put("/api/stocks/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "companyName": "",
                                  "exchange": "NSE",
                                  "active": true
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.detail")
                        .value(org.hamcrest.Matchers.containsString(
                                "companyName: must not be blank"
                        )));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void updateStock_shouldReturn404_whenStockDoesNotExist() throws Exception {
        when(stockService.updateStock(
                999999L,
                "Unknown Company",
                "NSE",
                true
        )).thenThrow(new StockNotFoundException(999999L));

        mockMvc.perform(put("/api/stocks/999999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "companyName": "Unknown Company",
                                  "exchange": "NSE",
                                  "active": true
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Stock not found"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getAllStocks_shouldReturn400_whenPageIsNegative() throws Exception {
        mockMvc.perform(get("/api/stocks?page=-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title")
                        .value("Invalid request parameter"))
                .andExpect(jsonPath("$.detail")
                        .value("page must be >= 0 and size must be between 1 and 100"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getAllStocks_shouldReturn400_whenSortFieldIsInvalid() throws Exception {
        mockMvc.perform(get("/api/stocks?sortBy=unknownField"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title")
                        .value("Invalid request parameter"))
                .andExpect(jsonPath("$.detail")
                        .value("sortBy must be one of: id, symbol, companyName, exchange, active"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getAllStocks_shouldReturn400_whenSortDirectionIsInvalid() throws Exception {
        mockMvc.perform(get("/api/stocks?direction=sideways"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title")
                        .value("Invalid request parameter"))
                .andExpect(jsonPath("$.detail")
                        .value("direction must be either 'asc' or 'desc'"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getStockById_shouldReturn500_whenUnexpectedExceptionOccurs()
            throws Exception {

        when(stockService.getStockById(1L))
                .thenThrow(new RuntimeException("internal database details"));

        mockMvc.perform(get("/api/stocks/1"))
                .andExpect(status().isInternalServerError());
    }

}
