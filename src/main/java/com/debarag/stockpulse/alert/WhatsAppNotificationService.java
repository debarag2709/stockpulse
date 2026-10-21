package com.debarag.stockpulse.alert;

import com.twilio.Twilio;
import com.twilio.exception.ApiException;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class WhatsAppNotificationService {

    private static final Logger log =
            LoggerFactory.getLogger(WhatsAppNotificationService.class);

    private final String accountSid;
    private final String authToken;
    private final String whatsappFrom;
    private final boolean enabled;

    public WhatsAppNotificationService(
            @Value("${twilio.account-sid}") String accountSid,
            @Value("${twilio.auth-token}") String authToken,
            @Value("${twilio.whatsapp-from}") String whatsappFrom,
            @Value("${twilio.whatsapp-enabled:false}") boolean enabled) {

        this.accountSid = accountSid;
        this.authToken = authToken;
        this.whatsappFrom = whatsappFrom;
        this.enabled = enabled;
    }

    public void sendPriceAlert(
            String recipientPhone,
            String stockSymbol,
            String condition,
            String targetPrice,
            String currentPrice) {

        if (!enabled) {
            log.debug("WhatsApp notifications are disabled.");
            return;
        }

        if (recipientPhone == null || recipientPhone.isBlank()) {
            log.warn("WhatsApp notification skipped: recipient phone is missing.");
            return;
        }

        if (stockSymbol == null || stockSymbol.isBlank()
                || condition == null || condition.isBlank()
                || targetPrice == null || currentPrice == null) {
            log.warn("WhatsApp notification skipped: required alert details are missing.");
            return;
        }

        String direction;
        if ("ABOVE".equalsIgnoreCase(condition)) {
            direction = "above";
        } else if ("BELOW".equalsIgnoreCase(condition)) {
            direction = "below";
        } else {
            log.warn("WhatsApp notification skipped: unsupported alert condition.");
            return;
        }

        String recipient = recipientPhone.startsWith("whatsapp:")
                ? recipientPhone
                : "whatsapp:" + recipientPhone;

        String body = """
                StockPulse Price Alert

                Stock: %s
                Condition: Price crossed %s target
                Target price: %s
                Current price: %s

                This is an automated stock-price alert.
                """.formatted(
                stockSymbol,
                direction,
                targetPrice,
                currentPrice
        );

        try {
            Twilio.init(accountSid, authToken);

            Message message = Message.creator(
                    new PhoneNumber(recipient),
                    new PhoneNumber(whatsappFrom),
                    body
            ).create();

            log.info(
                    "WhatsApp price alert accepted by Twilio. Stock: {}, message SID: {}, status: {}",
                    stockSymbol,
                    message.getSid(),
                    message.getStatus()
            );

        } catch (ApiException exception) {
            // Do not log credentials, recipient details, or message contents.
            log.error(
                    "Twilio rejected WhatsApp alert for stock {}. Error code: {}, message: {}",
                    stockSymbol,
                    exception.getCode(),
                    exception.getMessage()
            );

        } catch (Exception exception) {
            log.error(
                    "Failed to submit WhatsApp alert for stock {}. Exception type: {}",
                    stockSymbol,
                    exception.getClass().getSimpleName()
            );
        }
    }
}