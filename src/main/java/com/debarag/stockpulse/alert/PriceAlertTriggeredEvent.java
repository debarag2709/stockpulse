
package com.debarag.stockpulse.alert;

import java.math.BigDecimal;

public record PriceAlertTriggeredEvent(
        Long alertId,
        String recipientEmail,
        String stockSymbol,
        BigDecimal targetPrice,
        BigDecimal currentPrice,
        PriceAlert.AlertCondition condition
) {
}
