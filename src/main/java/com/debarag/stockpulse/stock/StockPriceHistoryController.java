
package com.debarag.stockpulse.stock;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/stocks/{stockId}/prices")
public class StockPriceHistoryController {

    private final StockPriceHistoryService priceHistoryService;

    public StockPriceHistoryController(
            StockPriceHistoryService priceHistoryService) {
        this.priceHistoryService = priceHistoryService;
    }

    @GetMapping("/latest")
    public PriceResponse getLatestPrice(@PathVariable Long stockId) {
        StockPriceHistory history = priceHistoryService
                .getLatestPrice(stockId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No price history found for stock ID " + stockId));

        return toResponse(history);
    }

    @GetMapping("/history")
    public List<PriceResponse> getPriceHistory(
            @PathVariable Long stockId) {
        return priceHistoryService.getPriceHistory(stockId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private PriceResponse toResponse(StockPriceHistory history) {
        return new PriceResponse(
                history.getId(),
                history.getStock().getId(),
                history.getPrice(),
                history.getRecordedAt()
        );
    }

    public record PriceResponse(
            Long id,
            Long stockId,
            BigDecimal price,
            OffsetDateTime recordedAt
    ) {}
}
