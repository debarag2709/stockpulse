
package com.debarag.stockpulse.stock;

public record StockResponse(
        Long id,
        String symbol,
        String companyName,
        String exchange,
        boolean active
) {
    public static StockResponse from(Stock stock) {
        return new StockResponse(
                stock.getId(),
                stock.getSymbol(),
                stock.getCompanyName(),
                stock.getExchange(),
                stock.isActive()
        );
    }
}
