
package com.debarag.stockpulse.user;

import com.debarag.stockpulse.stock.Stock;
import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(
        name = "watchlist",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_watchlist_user_stock",
                        columnNames = {"user_id", "stock_id"}
                )
        }
)
public class Watchlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    @Column(
            name = "created_at",
            nullable = false,
            insertable = false,
            updatable = false
    )
    private OffsetDateTime createdAt;

    protected Watchlist() {
        // Required by JPA
    }

    public Watchlist(User user, Stock stock) {
        this.user = user;
        this.stock = stock;
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

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
