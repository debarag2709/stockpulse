
package com.debarag.stockpulse.user;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private EntityManager entityManager;

    private BCryptPasswordEncoder passwordEncoder;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        authService = new AuthService(
                userRepository,
                passwordEncoder,
                entityManager
        );
    }

    @Test
    void register_shouldNormalizeEmailAndHashPassword() {
        when(userRepository.existsByEmail("test@example.com"))
                .thenReturn(false);

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        User result = authService.register(
                " TEST@EXAMPLE.COM ",
                "StrongPassword123!"
        );

        assertEquals("test@example.com", result.getEmail());
        assertNotEquals("StrongPassword123!", result.getPasswordHash());
        assertTrue(passwordEncoder.matches(
                "StrongPassword123!",
                result.getPasswordHash()
        ));

        verify(entityManager).flush();
        verify(entityManager).refresh(result);
    }

    @Test
    void register_shouldRejectDuplicateEmail() {
        when(userRepository.existsByEmail("test@example.com"))
                .thenReturn(true);

        assertThrows(
                IllegalArgumentException.class,
                () -> authService.register(
                        "test@example.com",
                        "StrongPassword123!"
                )
        );

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void authenticate_shouldReturnUserWithCorrectPassword() {
        User user = new User(
                "test@example.com",
                passwordEncoder.encode("StrongPassword123!")
        );

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));

        User result = authService.authenticate(
                " TEST@EXAMPLE.COM ",
                "StrongPassword123!"
        );

        assertSame(user, result);
    }

    @Test
    void authenticate_shouldRejectIncorrectPassword() {
        User user = new User(
                "test@example.com",
                passwordEncoder.encode("CorrectPassword123!")
        );

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));

        assertThrows(
                IllegalArgumentException.class,
                () -> authService.authenticate(
                        "test@example.com",
                        "WrongPassword123!"
                )
        );
    }

    @Test
    void authenticate_shouldRejectUnknownEmail() {
        when(userRepository.findByEmail("missing@example.com"))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> authService.authenticate(
                        "missing@example.com",
                        "SomePassword123!"
                )
        );
    }
}
