
package com.debarag.stockpulse.alert;

import com.debarag.stockpulse.user.User;
import com.debarag.stockpulse.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PriceAlertEmailListenerTest {

    @Mock
    private EmailNotificationService emailNotificationService;

    @Mock
    private WhatsAppNotificationService whatsAppNotificationService;

    @Mock
    private UserRepository userRepository;

    private PriceAlertEmailListener listener;

    @BeforeEach
    void setUp() {
        listener = new PriceAlertEmailListener(
                emailNotificationService,
                whatsAppNotificationService,
                userRepository
        );
    }

    @Test
    void shouldSendEmailAndSkipWhatsAppWhenNotConfigured() {
        PriceAlertTriggeredEvent event = createEvent(1L, "RELIANCE");

        User user = new User("investor@example.com", "hashed-password");

        when(userRepository.findByEmail(event.recipientEmail()))
                .thenReturn(Optional.of(user));

        listener.handlePriceAlertTriggered(event);

        verify(emailNotificationService).sendPriceAlertEmail(event);
        verify(userRepository).findByEmail(event.recipientEmail());
        verify(whatsAppNotificationService, never())
                .sendPriceAlert(
                        org.mockito.ArgumentMatchers.anyString(),
                        org.mockito.ArgumentMatchers.anyString(),
                        org.mockito.ArgumentMatchers.anyString(),
                        org.mockito.ArgumentMatchers.anyString(),
                        org.mockito.ArgumentMatchers.anyString()
                );
    }

    @Test
    void shouldSendWhatsAppWhenUserHasEnabledIt() {
        PriceAlertTriggeredEvent event = createEvent(2L, "TATASTEEL");

        User user = new User("investor@example.com", "hashed-password");
        user.setWhatsappPhone("+14155552671");
        user.setWhatsappEnabled(true);

        when(userRepository.findByEmail(event.recipientEmail()))
                .thenReturn(Optional.of(user));

        listener.handlePriceAlertTriggered(event);

        verify(emailNotificationService).sendPriceAlertEmail(event);
        verify(whatsAppNotificationService).sendPriceAlert(
                "+14155552671",
                "TATASTEEL",
                "ABOVE",
                "180.0000",
                "181.0000"
        );
    }

    @Test
    void shouldHandleEmailNotificationFailure() {
        PriceAlertTriggeredEvent event = createEvent(3L, "RELIANCE");

        doThrow(new RuntimeException("Simulated notification failure"))
                .when(emailNotificationService)
                .sendPriceAlertEmail(event);

        when(userRepository.findByEmail(event.recipientEmail()))
                .thenReturn(Optional.empty());

        assertDoesNotThrow(
                () -> listener.handlePriceAlertTriggered(event));

        verify(userRepository).findByEmail(event.recipientEmail());
    }

    @Test
    void shouldHandleMissingUserWithoutThrowing() {
        PriceAlertTriggeredEvent event = createEvent(4L, "INFY");

        when(userRepository.findByEmail(event.recipientEmail()))
                .thenReturn(Optional.empty());

        assertDoesNotThrow(
                () -> listener.handlePriceAlertTriggered(event));

        verify(emailNotificationService).sendPriceAlertEmail(event);
        verify(whatsAppNotificationService, never())
                .sendPriceAlert(
                        org.mockito.ArgumentMatchers.anyString(),
                        org.mockito.ArgumentMatchers.anyString(),
                        org.mockito.ArgumentMatchers.anyString(),
                        org.mockito.ArgumentMatchers.anyString(),
                        org.mockito.ArgumentMatchers.anyString()
                );
    }

    private PriceAlertTriggeredEvent createEvent(Long alertId, String symbol) {
        boolean tataSteel = "TATASTEEL".equals(symbol);

        return new PriceAlertTriggeredEvent(
                alertId,
                "investor@example.com",
                symbol,
                new BigDecimal(tataSteel ? "180.0000" : "1500.0000"),
                new BigDecimal(tataSteel ? "181.0000" : "1510.0000"),
                PriceAlert.AlertCondition.ABOVE
        );
    }
}
