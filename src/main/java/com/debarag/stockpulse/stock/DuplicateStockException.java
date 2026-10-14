package com.debarag.stockpulse.stock;

public class DuplicateStockException extends RuntimeException {

    public DuplicateStockException(String symbol) {
        super("Stock already exists: " + symbol);
    }
}