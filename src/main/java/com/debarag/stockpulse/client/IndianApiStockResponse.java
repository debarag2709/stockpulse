
package com.debarag.stockpulse.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record IndianApiStockResponse(
        String companyName,
        String exchangeCodeNse,
        String exchangeCodeBse,
        Map<String, String> currentPrice,
        String percentChange,
        String yearHigh,
        String yearLow
) {
}
