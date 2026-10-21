
package com.debarag.stockpulse.alert;

import com.debarag.stockpulse.stock.Stock;
import com.debarag.stockpulse.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "price_alerts")
public class PriceAlert {

    public enum AlertCondition {
        ABOVE,
        BELOW
    }

    public enum AlertStatus {
        ACTIVE,
        TRIGGERED,
        CANCELLED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    @Column(name = "target_price", nullable = false, precision = 15, scale = 4)
    private BigDecimal targetPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "condition", nullable = false, length = 10)
    private AlertCondition condition;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 15)
    private AlertStatus status = AlertStatus.ACTIVE;

    @Generated(event = EventType.INSERT)
    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "triggered_at")
    private OffsetDateTime triggeredAt;

    protected PriceAlert() {
        // Required by JPA
    }

    public PriceAlert(
            User user,
            Stock stock,
            BigDecimal targetPrice,
            AlertCondition condition) {
        this.user = user;
        this.stock = stock;
        this.targetPrice = targetPrice;
        this.condition = condition;
        this.status = AlertStatus.ACTIVE;
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public Stock getStock() {
        return stock;
    }

    public BigDecimal getTargetPrice() {
        return targetPrice;
    }

    public AlertCondition getCondition() {
        return condition;
    }

    public AlertStatus getStatus() {
        return status;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getTriggeredAt() {
        return triggeredAt;
    }

    public void trigger(OffsetDateTime triggeredAt) {
        if (this.status != AlertStatus.ACTIVE) {
            throw new IllegalStateException("Only active alerts can be triggered");
        }

        this.status = AlertStatus.TRIGGERED;
        this.triggeredAt = triggeredAt;
    }

    public void cancel() {
        if (this.status == AlertStatus.ACTIVE) {
            this.status = AlertStatus.CANCELLED;
        }
    }
}
