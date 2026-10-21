
package com.debarag.stockpulse.alert;

import com.debarag.stockpulse.stock.Stock;
import com.debarag.stockpulse.stock.StockRepository;
import com.debarag.stockpulse.user.User;
import com.debarag.stockpulse.user.UserRepository;
import jakarta.persistence.EntityManager;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Service
public class PriceAlertService {

    private final PriceAlertRepository priceAlertRepository;
    private final UserRepository userRepository;
    private final StockRepository stockRepository;
    private final EntityManager entityManager;
    private final ApplicationEventPublisher eventPublisher;

    public PriceAlertService(
            PriceAlertRepository priceAlertRepository,
            UserRepository userRepository,
            StockRepository stockRepository,
            EntityManager entityManager,
            ApplicationEventPublisher eventPublisher) {

        this.priceAlertRepository = priceAlertRepository;
        this.userRepository = userRepository;
        this.stockRepository = stockRepository;
        this.entityManager = entityManager;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public PriceAlert createAlert(
            Long userId,
            Long stockId,
            BigDecimal targetPrice,
            PriceAlert.AlertCondition condition) {

        if (targetPrice == null || targetPrice.signum() <= 0) {
            throw new IllegalArgumentException(
                    "Target price must be greater than zero");
        }

        if (condition == null) {
            throw new IllegalArgumentException(
                    "Alert condition is required");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"));

        Stock stock = stockRepository.findById(stockId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Stock not found"));

        PriceAlert alert = priceAlertRepository.save(
                new PriceAlert(user, stock, targetPrice, condition));

        entityManager.flush();
        entityManager.refresh(alert);

        return alert;
    }

    @Transactional(readOnly = true)
    public List<PriceAlert> getAlerts(Long userId) {

        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("User not found");
        }

        return priceAlertRepository
                .findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional
    public PriceAlert cancelAlert(Long userId, Long alertId) {

        PriceAlert alert = priceAlertRepository
                .findByIdAndUserId(alertId, userId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Alert not found for this user"));

        if (alert.getStatus() != PriceAlert.AlertStatus.ACTIVE) {
            throw new IllegalStateException(
                    "Only active alerts can be cancelled");
        }

        alert.cancel();

        return priceAlertRepository.save(alert);
    }

    @Transactional
    public int evaluateAlerts(Long stockId, BigDecimal currentPrice) {

        if (currentPrice == null || currentPrice.signum() < 0) {
            throw new IllegalArgumentException(
                    "Current price must be zero or greater");
        }

        List<PriceAlert> alerts = priceAlertRepository
                .findByStockIdAndStatus(
                        stockId,
                        PriceAlert.AlertStatus.ACTIVE);

        int triggeredCount = 0;
        OffsetDateTime now = OffsetDateTime.now();

        for (PriceAlert alert : alerts) {

            boolean reached = switch (alert.getCondition()) {
                case ABOVE ->
                        currentPrice.compareTo(alert.getTargetPrice()) >= 0;
                case BELOW ->
                        currentPrice.compareTo(alert.getTargetPrice()) <= 0;
            };

            if (reached) {
                alert.trigger(now);

                eventPublisher.publishEvent(
                        new PriceAlertTriggeredEvent(
                                alert.getId(),
                                alert.getUser().getEmail(),
                                alert.getStock().getSymbol(),
                                alert.getTargetPrice(),
                                currentPrice,
                                alert.getCondition()
                        )
                );

                triggeredCount++;
            }
        }

        return triggeredCount;
    }
}
