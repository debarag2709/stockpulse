
package com.debarag.stockpulse.stock;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.PathVariable;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@RestController
@RequestMapping("/api/stocks")
public class StockController {

    private final StockService stockService;

    public StockController(StockService stockService) {
        this.stockService = stockService;
    }

    public record CreateStockRequest(
            @NotBlank @Size(max = 20) String symbol,
            @NotBlank @Size(max = 150) String companyName,
            @NotBlank @Size(max = 20) String exchange
    ) {}


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StockResponse createStock(
            @Valid @RequestBody CreateStockRequest request
    ) {
        Stock stock = stockService.createStock(
                request.symbol(),
                request.companyName(),
                request.exchange()
        );

        return StockResponse.from(stock);
    }


    public record UpdateStockRequest(
            @NotBlank @Size(max = 150) String companyName,
            @NotBlank @Size(max = 20) String exchange,
            boolean active
    ) {}



    @GetMapping
    public Page<StockResponse> getAllStocks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {

        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException(
                    "page must be >= 0 and size must be between 1 and 100"
            );
        }


        if (!List.of("id", "symbol", "companyName", "exchange", "active")
                .contains(sortBy)) {
            throw new IllegalArgumentException(
                    "sortBy must be one of: id, symbol, companyName, exchange, active"
            );
        }

        Sort.Direction sortDirection;
        try {
            sortDirection = Sort.Direction.fromString(direction);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "direction must be either 'asc' or 'desc'"
            );
        }

        Pageable pageable = org.springframework.data.domain.PageRequest.of(
                page,
                size,
                Sort.by(sortDirection, sortBy)
        );

        return stockService.getAllStocks(pageable)
                .map(StockResponse::from);
    }


    @GetMapping("/{id}")
    public StockResponse getStockById(@PathVariable Long id) {
        return StockResponse.from(stockService.getStockById(id));
    }


    @PutMapping("/{id}")
    public StockResponse updateStock(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStockRequest request) {

        Stock updatedStock = stockService.updateStock(
                id,
                request.companyName(),
                request.exchange(),
                request.active()
        );

        return StockResponse.from(updatedStock);
    }

}
