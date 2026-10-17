
package com.debarag.stockpulse.stock;

import com.debarag.stockpulse.client.IndianApiClient;
import com.debarag.stockpulse.client.IndianApiStockResponse;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class StockPriceSyncService {

    private final StockRepository stockRepository;
    private final IndianApiClient indianApiClient;
    private final StockPriceHistoryService priceHistoryService;

    public StockPriceSyncService(
            StockRepository stockRepository,
            IndianApiClient indianApiClient,
            StockPriceHistoryService priceHistoryService) {
        this.stockRepository = stockRepository;
        this.indianApiClient = indianApiClient;
        this.priceHistoryService = priceHistoryService;
    }

    public int syncActiveStocks() {
        List<Stock> stocks = stockRepository.findAll()
                .stream()
                .filter(Stock::isActive)
                .toList();

        int recordedCount = 0;

        for (Stock stock : stocks) {
            try {
                IndianApiStockResponse response =
                        indianApiClient.getStockData(stock.getCompanyName());

                if (response == null || response.currentPrice() == null) {
                    System.out.println(
                            "No price data returned for stock ID "
                                    + stock.getId()
                                    + ", symbol " + stock.getSymbol());
                    continue;
                }

                String exchange = stock.getExchange()
                        .trim()
                        .toUpperCase(Locale.ROOT);

                Map<String, String> prices = response.currentPrice();
                System.out.println(
                        "Syncing symbol=" + stock.getSymbol()
                                + ", company=" + stock.getCompanyName()
                                + ", exchange=" + exchange
                                + ", providerPrices=" + prices
                );

                String priceText = prices.get(exchange);

                if (priceText == null || priceText.isBlank()) {
                    System.out.println(
                            "No " + exchange + " price returned for stock ID "
                                    + stock.getId()
                                    + ", symbol " + stock.getSymbol()
                                    + ". Available exchanges: "
                                    + prices.keySet());
                    continue;
                }

                BigDecimal price = new BigDecimal(priceText.trim());

                if (price.signum() < 0) {
                    System.out.println(
                            "Ignoring negative price for stock ID "
                                    + stock.getId());
                    continue;
                }

                priceHistoryService.recordPrice(stock.getId(), price);
                recordedCount++;

                System.out.println(
                        "Recorded price for " + stock.getSymbol()
                                + " (" + exchange + "): " + price);

            } catch (RuntimeException exception) {
                // One stock's failure should not stop the remaining stocks.
                System.err.println(
                        "Failed to sync stock ID " + stock.getId()
                                + ", symbol " + stock.getSymbol()
                                + ": " + exception.getMessage());
            }
        }

        return recordedCount;
    }
}
