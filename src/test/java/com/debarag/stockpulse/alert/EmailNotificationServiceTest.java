
package com.debarag.stockpulse.alert;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class EmailNotificationServiceTest {

    private EmailNotificationService emailNotificationService;
    private MockRestServiceServer mockServer;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();

        mockServer = MockRestServiceServer
                .bindTo(builder)
                .build();

        emailNotificationService =
                new EmailNotificationService("test-resend-api-key", builder);
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

        mockServer.expect(requestTo("https://api.resend.com/emails"))
                .andExpect(method(org.springframework.http.HttpMethod.POST))
                .andExpect(header(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer test-resend-api-key"))
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON))
                .andExpect(content().string(
                        org.hamcrest.Matchers.containsString(
                                "investor@example.com")))
                .andExpect(content().string(
                        org.hamcrest.Matchers.containsString(
                                "StockPulse Price Alert: RELIANCE")))
                .andExpect(content().string(
                        org.hamcrest.Matchers.containsString("1510.0000")))
                .andRespond(withSuccess());

        assertDoesNotThrow(
                () -> emailNotificationService.sendPriceAlertEmail(event));

        mockServer.verify();
    }

    @Test
    void shouldThrowWhenResendDeliveryFails() {

        PriceAlertTriggeredEvent event = new PriceAlertTriggeredEvent(
                2L,
                "investor@example.com",
                "TATASTEEL",
                new BigDecimal("180.0000"),
                new BigDecimal("181.0000"),
                PriceAlert.AlertCondition.ABOVE
        );

        mockServer.expect(requestTo("https://api.resend.com/emails"))
                .andExpect(method(org.springframework.http.HttpMethod.POST))
                .andRespond(withServerError());

        assertThrows(
                RestClientException.class,
                () -> emailNotificationService.sendPriceAlertEmail(event)
        );

        mockServer.verify();
    }
}
