
package com.debarag.stockpulse.alert;

import com.debarag.stockpulse.user.User;
import com.debarag.stockpulse.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class PriceAlertEmailListener {

    private static final Logger log =
            LoggerFactory.getLogger(PriceAlertEmailListener.class);

    private final EmailNotificationService emailNotificationService;
    private final WhatsAppNotificationService whatsAppNotificationService;
    private final UserRepository userRepository;

    public PriceAlertEmailListener(
            EmailNotificationService emailNotificationService,
            WhatsAppNotificationService whatsAppNotificationService,
            UserRepository userRepository) {
        this.emailNotificationService = emailNotificationService;
        this.whatsAppNotificationService = whatsAppNotificationService;
        this.userRepository = userRepository;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePriceAlertTriggered(PriceAlertTriggeredEvent event) {
        log.info(
                "Processing notifications for price alert ID {}",
                event.alertId());

        // Email delivery remains independent of WhatsApp delivery.
        try {
            emailNotificationService.sendPriceAlertEmail(event);
        } catch (RuntimeException exception) {
            log.error(
                    "Failed to process email notification for alert ID {}",
                    event.alertId(),
                    exception);
        }

        // Send WhatsApp only when the user has enabled it and configured a number.
        try {
            User user = userRepository.findByEmail(event.recipientEmail())
                    .orElse(null);

            if (user == null) {
                log.warn(
                        "WhatsApp skipped: user not found for alert ID {}",
                        event.alertId());
                return;
            }

            if (!user.isWhatsappEnabled()
                    || user.getWhatsappPhone() == null
                    || user.getWhatsappPhone().isBlank()) {
                log.debug(
                        "WhatsApp notifications are not configured for alert ID {}",
                        event.alertId());
                return;
            }

            whatsAppNotificationService.sendPriceAlert(
                    user.getWhatsappPhone(),
                    event.stockSymbol(),
                    event.condition().name(),
                    event.targetPrice().toPlainString(),
                    event.currentPrice().toPlainString());

        } catch (RuntimeException exception) {
            log.error(
                    "Failed to process WhatsApp notification for alert ID {}",
                    event.alertId(),
                    exception);
        }
    }
}
