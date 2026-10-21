
package com.debarag.stockpulse.alert;

import com.debarag.stockpulse.stock.Stock;
import com.debarag.stockpulse.stock.StockRepository;
import com.debarag.stockpulse.user.User;
import com.debarag.stockpulse.user.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class PriceAlertServiceTest {

    @Mock
    private PriceAlertRepository priceAlertRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private UserRepository userRepository;

    @Mock
    private StockRepository stockRepository;

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private PriceAlertService priceAlertService;

    private User user;
    private Stock stock;

    @BeforeEach
    void setUp() {
        user = mock(User.class);
        stock = mock(Stock.class);
    }

    @Test
    void shouldTriggerAboveAlertWhenPriceReachesTarget() {
        PriceAlert alert = new PriceAlert(
                user,
                stock,
                new BigDecimal("1500"),
                PriceAlert.AlertCondition.ABOVE);

        when(priceAlertRepository.findByStockIdAndStatus(
                1L, PriceAlert.AlertStatus.ACTIVE))
                .thenReturn(List.of(alert));

        int triggered = priceAlertService.evaluateAlerts(
                1L, new BigDecimal("1500"));

        assertEquals(1, triggered);
        assertEquals(
                PriceAlert.AlertStatus.TRIGGERED,
                alert.getStatus());

        verify(priceAlertRepository).findByStockIdAndStatus(
                1L, PriceAlert.AlertStatus.ACTIVE);
    }

    @Test
    void shouldNotTriggerAboveAlertWhenPriceIsBelowTarget() {
        PriceAlert alert = new PriceAlert(
                user,
                stock,
                new BigDecimal("1500"),
                PriceAlert.AlertCondition.ABOVE);

        when(priceAlertRepository.findByStockIdAndStatus(
                1L, PriceAlert.AlertStatus.ACTIVE))
                .thenReturn(List.of(alert));

        int triggered = priceAlertService.evaluateAlerts(
                1L, new BigDecimal("1499"));

        assertEquals(0, triggered);
        assertEquals(
                PriceAlert.AlertStatus.ACTIVE,
                alert.getStatus());
    }

    @Test
    void shouldTriggerBelowAlertWhenPriceReachesTarget() {
        PriceAlert alert = new PriceAlert(
                user,
                stock,
                new BigDecimal("1400"),
                PriceAlert.AlertCondition.BELOW);

        when(priceAlertRepository.findByStockIdAndStatus(
                1L, PriceAlert.AlertStatus.ACTIVE))
                .thenReturn(List.of(alert));

        int triggered = priceAlertService.evaluateAlerts(
                1L, new BigDecimal("1400"));

        assertEquals(1, triggered);
        assertEquals(
                PriceAlert.AlertStatus.TRIGGERED,
                alert.getStatus());
    }

    @Test
    void shouldNotTriggerAlertMoreThanOnce() {
        PriceAlert alert = new PriceAlert(
                user,
                stock,
                new BigDecimal("1500"),
                PriceAlert.AlertCondition.ABOVE);

        when(priceAlertRepository.findByStockIdAndStatus(
                1L, PriceAlert.AlertStatus.ACTIVE))
                .thenReturn(List.of(alert), List.of());

        int firstResult = priceAlertService.evaluateAlerts(
                1L, new BigDecimal("1500"));
        int secondResult = priceAlertService.evaluateAlerts(
                1L, new BigDecimal("1600"));

        assertEquals(1, firstResult);
        assertEquals(0, secondResult);
        assertEquals(
                PriceAlert.AlertStatus.TRIGGERED,
                alert.getStatus());
    }


    @Test
    void shouldNotCancelAnotherUsersAlert() {
        when(priceAlertRepository.findByIdAndUserId(1L, 2L))
                .thenReturn(java.util.Optional.empty());

        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> priceAlertService.cancelAlert(2L, 1L));

        verify(priceAlertRepository).findByIdAndUserId(1L, 2L);
        verify(priceAlertRepository, never()).save(any(PriceAlert.class));
    }

}
