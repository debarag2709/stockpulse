
package com.debarag.stockpulse.alert;

import com.debarag.stockpulse.stock.Stock;
import com.debarag.stockpulse.user.User;
import com.debarag.stockpulse.user.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/alerts")
public class PriceAlertController {

    private final PriceAlertService priceAlertService;
    private final UserRepository userRepository;

    public PriceAlertController(
            PriceAlertService priceAlertService,
            UserRepository userRepository) {
        this.priceAlertService = priceAlertService;
        this.userRepository = userRepository;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AlertResponse createAlert(
            @Valid @RequestBody CreateAlertRequest request,
            Authentication authentication) {

        User user = getAuthenticatedUser(authentication);

        PriceAlert alert = priceAlertService.createAlert(
                user.getId(),
                request.stockId(),
                request.targetPrice(),
                request.condition());

        return toResponse(alert);
    }

    @GetMapping
    public List<AlertResponse> getAlerts(Authentication authentication) {
        User user = getAuthenticatedUser(authentication);

        return priceAlertService.getAlerts(user.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @DeleteMapping("/{alertId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelAlert(
            @PathVariable Long alertId,
            Authentication authentication) {

        User user = getAuthenticatedUser(authentication);
        priceAlertService.cancelAlert(user.getId(), alertId);
    }

    private User getAuthenticatedUser(Authentication authentication) {
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"));
    }

    private AlertResponse toResponse(PriceAlert alert) {
        Stock stock = alert.getStock();

        return new AlertResponse(
                alert.getId(),
                stock.getId(),
                stock.getSymbol(),
                stock.getCompanyName(),
                alert.getTargetPrice(),
                alert.getCondition(),
                alert.getStatus(),
                alert.getCreatedAt(),
                alert.getTriggeredAt());
    }

    public record CreateAlertRequest(
            @NotNull Long stockId,
            @NotNull @DecimalMin(value = "0.0001") BigDecimal targetPrice,
            @NotNull PriceAlert.AlertCondition condition) {}

    public record AlertResponse(
            Long id,
            Long stockId,
            String symbol,
            String companyName,
            BigDecimal targetPrice,
            PriceAlert.AlertCondition condition,
            PriceAlert.AlertStatus status,
            OffsetDateTime createdAt,
            OffsetDateTime triggeredAt) {}
}
