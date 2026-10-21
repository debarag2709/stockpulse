
package com.debarag.stockpulse.alert;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailNotificationServiceTest {

    @Mock
    private JavaMailSender mailSender;

    private EmailNotificationService emailNotificationService;

    @BeforeEach
    void setUp() {
        emailNotificationService =
                new EmailNotificationService(mailSender);
    }

    @Test
    void shouldSendPriceAlertEmail() {
        PriceAlertTriggeredEvent event = new PriceAlertTriggeredEvent(
                1L,
                "investor@example.com",
                "RELIANCE",
                new BigDecimal("1500.0000"),
                new BigDecimal("1510.0000"),
                PriceAlert.AlertCondition.ABOVE
        );

        emailNotificationService.sendPriceAlertEmail(event);

        ArgumentCaptor<SimpleMailMessage> captor =
                ArgumentCaptor.forClass(SimpleMailMessage.class);

        verify(mailSender).send(captor.capture());

        SimpleMailMessage message = captor.getValue();

        assertArrayEquals(
                new String[]{"investor@example.com"},
                message.getTo());
        assertEquals(
                "StockPulse Price Alert: RELIANCE",
                message.getSubject());
        assertTrue(message.getText().contains("1510.0000"));
        assertTrue(message.getText().contains("1500.0000"));
        assertTrue(message.getText().contains("reached or exceeded"));
    }

    @Test
    void shouldNotThrowWhenEmailDeliveryFails() {
        PriceAlertTriggeredEvent event = new PriceAlertTriggeredEvent(
                2L,
                "investor@example.com",
                "TATASTEEL",
                new BigDecimal("180.0000"),
                new BigDecimal("181.0000"),
                PriceAlert.AlertCondition.ABOVE
        );

        doThrow(new org.springframework.mail.MailSendException(
                "Simulated SMTP failure"))
                .when(mailSender)
                .send(any(SimpleMailMessage.class));

        assertDoesNotThrow(
                () -> emailNotificationService.sendPriceAlertEmail(event));

        verify(mailSender).send(any(SimpleMailMessage.class));
    }
}
