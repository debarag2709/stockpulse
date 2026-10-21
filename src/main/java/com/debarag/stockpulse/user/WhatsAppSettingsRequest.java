
package com.debarag.stockpulse.user;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record WhatsAppSettingsRequest(

        @Pattern(
                regexp = "^\\+[1-9]\\d{7,14}$",
                message = "Phone number must use international format, e.g. +14155552671"
        )
        String phone,

        @NotNull(message = "enabled is required")
        Boolean enabled

) {}
