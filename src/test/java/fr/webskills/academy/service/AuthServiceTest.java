package fr.webskills.academy.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import fr.webskills.academy.domain.AccessCode;
import fr.webskills.academy.dto.AuthDtos.AuthResponse;
import fr.webskills.academy.repository.AccessCodeRepository;
import fr.webskills.academy.repository.UserRepository;
import fr.webskills.academy.security.JwtService;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock AccessCodeRepository codeRepo;
    @Mock UserRepository userRepo;
    @Mock PasswordEncoder encoder;
    @Mock JwtService jwt;
    @Mock AuthenticationManager authManager;

    @Test
    void class_code_login_returns_class_home_metadata_with_access_dates() {
        UUID accessCodeId = UUID.randomUUID();
        Instant startsAt = Instant.now().minusSeconds(3600);
        Instant expiresAt = Instant.now().plusSeconds(86400);
        AccessCode accessCode = new AccessCode();
        accessCode.setId(accessCodeId);
        accessCode.setCode("WSA-CLASS");
        accessCode.setLabel("Promo HTML 2026");
        accessCode.setWelcomeMessage("Bienvenue la promo HTML");
        accessCode.setRecommendedPath("Parcours conseillé : HTML puis CSS");
        accessCode.setStartsAt(startsAt);
        accessCode.setExpiresAt(expiresAt);
        accessCode.setActive(true);

        when(codeRepo.findByCode("WSA-CLASS")).thenReturn(Optional.of(accessCode));
        when(jwt.generate(accessCode)).thenReturn("token");

        AuthService service = new AuthService(codeRepo, userRepo, encoder, jwt, authManager);

        AuthResponse response = service.authenticateWithCode("WSA-CLASS");

        assertThat(response.accessCodeId()).isEqualTo(accessCodeId);
        assertThat(response.accessCodeLabel()).isEqualTo("Promo HTML 2026");
        assertThat(response.welcomeMessage()).isEqualTo("Bienvenue la promo HTML");
        assertThat(response.recommendedPath()).isEqualTo("Parcours conseillé : HTML puis CSS");
        assertThat(response.startsAt()).isEqualTo(startsAt);
        assertThat(response.expiresAt()).isEqualTo(expiresAt);
    }
}
