
package com.debarag.stockpulse.user;

import com.debarag.stockpulse.config.JwtService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;

    public AuthController(AuthService authService, JwtService jwtService) {
        this.authService = authService;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    @org.springframework.web.bind.annotation.ResponseStatus(HttpStatus.CREATED)
    public RegistrationResponse register(
            @Valid @RequestBody RegisterRequest request
    ) {
        try {
            User user = authService.register(
                    request.email(),
                    request.password()
            );

            return new RegistrationResponse(
                    user.getId(),
                    user.getEmail(),
                    user.getCreatedAt()
            );
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    ex.getMessage()
            );
        }
    }



    @PostMapping("/login")
    public LoginResponse login(
            @Valid @RequestBody LoginRequest request
    ) {
        try {
            User user = authService.authenticate(
                    request.email(),
                    request.password()
            );

            String token = jwtService.generateToken(
                    user.getId(),
                    user.getEmail()
            );

            return new LoginResponse(
                    user.getId(),
                    user.getEmail(),
                    token,
                    "Bearer"
            );
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid email or password"
            );
        }
    }

    public record LoginResponse(
            Long id,
            String email,
            String accessToken,
            String tokenType
    ) {}



    public record RegistrationResponse(
            Long id,
            String email,
            java.time.OffsetDateTime createdAt
    ) {
    }
}
