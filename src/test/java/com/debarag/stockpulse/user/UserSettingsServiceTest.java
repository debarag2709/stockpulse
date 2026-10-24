
package com.debarag.stockpulse.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserSettingsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserSettingsService userSettingsService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User("test@example.com", "hashed-password");
    }

    @Test
    void getSettings_shouldReturnUser_whenUserExists() {
        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));
        User result = userSettingsService.getSettings("test@example.com");

        assertSame(user, result);
        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    void getSettings_shouldNormalizeEmail() {
        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));

        User result = userSettingsService.getSettings(" TEST@EXAMPLE.COM ");

        assertSame(user, result);
        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    void getSettings_shouldThrowException_whenUserDoesNotExist() {
        when(userRepository.findByEmail("missing@example.com"))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> userSettingsService.getSettings("missing@example.com")
        );
    }

    @Test
    void updateWhatsAppSettings_shouldSaveValidPhoneNumber() {
        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));

        when(userRepository.save(user))
                .thenReturn(user);
        User result = userSettingsService.updateWhatsAppSettings(
                "test@example.com",
                "+14155552671",
                true
        );

        assertEquals("+14155552671", result.getWhatsappPhone());
        assertTrue(result.isWhatsappEnabled());
        verify(userRepository).save(user);
    }

    @Test
    void updateWhatsAppSettings_shouldRejectInvalidPhoneNumber() {
        assertThrows(
                IllegalArgumentException.class,
                () -> userSettingsService.updateWhatsAppSettings(
                        "test@example.com",
                        "12345",
                        true
                )
        );

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateWhatsAppSettings_shouldRejectEnabledWithoutPhone() {
        assertThrows(
                IllegalArgumentException.class,
                () -> userSettingsService.updateWhatsAppSettings(
                        "test@example.com",
                        null,
                        true
                )
        );

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateWhatsAppSettings_shouldAllowDisablingNotifications() {
        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));

        when(userRepository.save(user))
                .thenReturn(user);
        User result = userSettingsService.updateWhatsAppSettings(
                "test@example.com",
                null,
                false
        );

        assertFalse(result.isWhatsappEnabled());
        verify(userRepository).save(user);
    }
}
