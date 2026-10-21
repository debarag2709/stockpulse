
package com.debarag.stockpulse.alert;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PriceAlertRepository extends JpaRepository<PriceAlert, Long> {

    @EntityGraph(attributePaths = {"stock"})
    List<PriceAlert> findByUserIdOrderByCreatedAtDesc(Long userId);

    @EntityGraph(attributePaths = {"stock"})
    List<PriceAlert> findByStockIdAndStatus(
            Long stockId,
            PriceAlert.AlertStatus status);

    @EntityGraph(attributePaths = {"stock"})
    Optional<PriceAlert> findByIdAndUserId(Long id, Long userId);
}