
package com.debarag.stockpulse.alert;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailNotificationService {

    private static final Logger log =
            LoggerFactory.getLogger(EmailNotificationService.class);

    private final JavaMailSender mailSender;

    public EmailNotificationService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendPriceAlertEmail(PriceAlertTriggeredEvent event) {
        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(event.recipientEmail());
        message.setSubject(
                "StockPulse Price Alert: " + event.stockSymbol());

        String conditionText = event.condition() == PriceAlert.AlertCondition.ABOVE
                ? "reached or exceeded"
                : "reached or fallen below";

        message.setText("""
                Your StockPulse price alert has been triggered.

                Stock: %s
                Condition: %s
                Target price: ₹%s
                Current price: ₹%s

                This price was reported by your configured market-data provider.
                Prices may be delayed.

                Regards,
                StockPulse
                """.formatted(
                event.stockSymbol(),
                conditionText,
                event.targetPrice().toPlainString(),
                event.currentPrice().toPlainString()
        ));

        try {
            mailSender.send(message);
            log.info(
                    "Price alert email sent for alert ID {}",
                    event.alertId());
        } catch (MailException exception) {
            log.error(
                    "Failed to send email for price alert ID {}",
                    event.alertId(),
                    exception);
        }
    }
}
