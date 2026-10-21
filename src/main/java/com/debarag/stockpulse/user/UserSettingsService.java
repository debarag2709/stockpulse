
package com.debarag.stockpulse.user;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserSettingsService {

    private final UserRepository userRepository;

    public UserSettingsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public User getSettings(String email) {
        return userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"));
    }

    @Transactional
    public User updateWhatsAppSettings(
            String email,
            String phone,
            boolean enabled) {

        User user = getSettingsForUpdate(email);

        if (enabled && (phone == null || phone.isBlank())) {
            throw new IllegalArgumentException(
                    "A WhatsApp phone number is required when notifications are enabled");
        }

        if (phone != null && !phone.isBlank()) {
            String normalizedPhone = phone.trim();

            if (!normalizedPhone.matches("^\\+[1-9]\\d{7,14}$")) {
                throw new IllegalArgumentException(
                        "Phone number must use international format, such as +14155552671");
            }

            user.setWhatsappPhone(normalizedPhone);
        }

        user.setWhatsappEnabled(enabled);
        return userRepository.save(user);
    }

    private User getSettingsForUpdate(String email) {
        return userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"));
    }
}
