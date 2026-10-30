
package com.debarag.stockpulse.alert;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Map;

@Service
public class EmailNotificationService {

    private static final Logger log =
            LoggerFactory.getLogger(EmailNotificationService.class);

    private static final String RESEND_API_URL =
            "https://api.resend.com/emails";

    private final RestClient restClient;
    private final String apiKey;

    public EmailNotificationService(
            @Value("${RESEND_API_KEY:}") String apiKey,
            RestClient.Builder restClientBuilder) {
        this.apiKey = apiKey;
        this.restClient = restClientBuilder.build();
    }

    public void sendPriceAlertEmail(PriceAlertTriggeredEvent event) {

        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "RESEND_API_KEY environment variable is not configured");
        }

        String conditionText =
                event.condition() == PriceAlert.AlertCondition.ABOVE
                        ? "reached or exceeded"
                        : "reached or fallen below";

        String subject =
                "StockPulse Price Alert: " + event.stockSymbol();

        String text = """
                Your StockPulse price alert has been triggered.

                Stock: %s
                Condition: %s
                Target price: Rs. %s
                Current price: Rs. %s

                This price was reported by your configured market-data provider.
                Prices may be delayed.

                Regards,
                StockPulse
                """.formatted(
                event.stockSymbol(),
                conditionText,
                event.targetPrice().toPlainString(),
                event.currentPrice().toPlainString()
        );

        String html = """
                <h2>StockPulse Price Alert</h2>
                <p>Your price alert has been triggered.</p>
                <p><b>Stock:</b> %s</p>
                <p><b>Condition:</b> %s</p>
                <p><b>Target price:</b> Rs. %s</p>
                <p><b>Current price:</b> Rs. %s</p>
                <p>Prices may be delayed.</p>
                <p>Regards,<br/>StockPulse</p>
                """.formatted(
                event.stockSymbol(),
                conditionText,
                event.targetPrice().toPlainString(),
                event.currentPrice().toPlainString()
        );

        Map<String, Object> requestBody = Map.of(
                "from", "StockPulse <onboarding@resend.dev>",
                "to", List.of(event.recipientEmail()),
                "subject", subject,
                "text", text,
                "html", html
        );

        try {
            restClient.post()
                    .uri(RESEND_API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + apiKey)
                    .body(requestBody)
                    .retrieve()
                    .toBodilessEntity();

            log.info(
                    "Price alert email accepted by Resend for alert ID {}",
                    event.alertId());

        } catch (RestClientException exception) {
            log.error(
                    "Resend email request failed for alert ID {}",
                    event.alertId(),
                    exception);
            throw exception;
        }
    }
}
