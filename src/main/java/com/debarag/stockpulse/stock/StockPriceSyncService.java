
package com.debarag.stockpulse.stock;

import com.debarag.stockpulse.alert.PriceAlertService;
import com.debarag.stockpulse.client.IndianApiClient;
import com.debarag.stockpulse.client.IndianApiStockResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class StockPriceSyncService {

    private static final Logger log =
            LoggerFactory.getLogger(StockPriceSyncService.class);

    private final StockRepository stockRepository;
    private final IndianApiClient indianApiClient;
    private final StockPriceHistoryService priceHistoryService;
    private final PriceAlertService priceAlertService;

    public StockPriceSyncService(
            StockRepository stockRepository,
            IndianApiClient indianApiClient,
            StockPriceHistoryService priceHistoryService,
            PriceAlertService priceAlertService) {
        this.stockRepository = stockRepository;
        this.indianApiClient = indianApiClient;
        this.priceHistoryService = priceHistoryService;
        this.priceAlertService = priceAlertService;
    }

    /**
     * Sync all active stocks. Used by the scheduler.
     */
    public int syncActiveStocks() {
        List<Stock> stocks = stockRepository.findAll()
                .stream()
                .filter(Stock::isActive)
                .toList();

        int recordedCount = 0;

        for (Stock stock : stocks) {
            if (syncStock(stock)) {
                recordedCount++;
            }
        }

        return recordedCount;
    }

    /**
     * Sync one stock immediately after creation.
     */
    public boolean syncStockById(Long stockId) {
        return stockRepository.findById(stockId)
                .map(this::syncStock)
                .orElseGet(() -> {
                    log.warn(
                            "Cannot sync price: stock ID {} not found",
                            stockId
                    );
                    return false;
                });
    }

    private boolean syncStock(Stock stock) {
        try {
            IndianApiStockResponse response =
                    indianApiClient.getStockData(stock.getSymbol());

            if (response == null || response.currentPrice() == null) {
                log.warn(
                        "No price data returned for stock {} ({}).",
                        stock.getSymbol(),
                        stock.getId()
                );
                return false;
            }

            String exchange = stock.getExchange()
                    .trim()
                    .toUpperCase(Locale.ROOT);

            Map<String, String> prices = response.currentPrice();
            String priceText = prices.get(exchange);

            if (priceText == null || priceText.isBlank()) {
                log.warn(
                        "No {} price returned for stock {}. Available exchanges: {}",
                        exchange,
                        stock.getSymbol(),
                        prices.keySet()
                );
                return false;
            }

            BigDecimal price = new BigDecimal(priceText.trim());

            if (price.signum() < 0) {
                log.warn(
                        "Ignoring negative price for stock {}",
                        stock.getSymbol()
                );
                return false;
            }

            priceHistoryService.recordPrice(stock.getId(), price);

            log.info(
                    "Recorded price for {} ({}): {}",
                    stock.getSymbol(),
                    exchange,
                    price
            );

            try {
                int triggeredCount =
                        priceAlertService.evaluateAlerts(
                                stock.getId(),
                                price
                        );

                if (triggeredCount > 0) {
                    log.info(
                            "Triggered {} price alert(s) for {}",
                            triggeredCount,
                            stock.getSymbol()
                    );
                }
            } catch (RuntimeException alertException) {
                log.error(
                        "Price was saved, but alert evaluation failed for {}",
                        stock.getSymbol(),
                        alertException
                );
            }

            return true;

        } catch (RuntimeException exception) {
            log.error(
                    "Failed to sync stock {} ({})",
                    stock.getSymbol(),
                    stock.getId(),
                    exception
            );
            return false;
        }
    }
}
