
package com.debarag.stockpulse.stock;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "stock_price_history")
public class StockPriceHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    @Column(name = "price", nullable = false, precision = 15, scale = 4)
    private BigDecimal price;

    @Generated(event = EventType.INSERT)
    @Column(name = "recorded_at", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime recordedAt;

    protected StockPriceHistory() {
    }

    public StockPriceHistory(Stock stock, BigDecimal price) {
        this.stock = stock;
        this.price = price;
    }

    public Long getId() {
        return id;
    }

    public Stock getStock() {
        return stock;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public OffsetDateTime getRecordedAt() {
        return recordedAt;
    }
}
