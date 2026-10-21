
package com.debarag.stockpulse.user;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/users/me/whatsapp")
public class UserSettingsController {

    private final UserSettingsService userSettingsService;

    public UserSettingsController(UserSettingsService userSettingsService) {
        this.userSettingsService = userSettingsService;
    }

    @GetMapping
    public Map<String, Object> getWhatsAppSettings(
            Authentication authentication) {

        User user = userSettingsService.getSettings(authentication.getName());

        return Map.of(
                "enabled", user.isWhatsappEnabled(),
                "phoneConfigured", user.getWhatsappPhone() != null
        );
    }

    @PutMapping
    public Map<String, Object> updateWhatsAppSettings(
            Authentication authentication,
            @Valid @RequestBody WhatsAppSettingsRequest request) {

        try {
            User user = userSettingsService.updateWhatsAppSettings(
                    authentication.getName(),
                    request.phone(),
                    request.enabled()
            );

            return Map.of(
                    "enabled", user.isWhatsappEnabled(),
                    "phoneConfigured", user.getWhatsappPhone() != null
            );
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    exception.getMessage()
            );
        }
    }
}
