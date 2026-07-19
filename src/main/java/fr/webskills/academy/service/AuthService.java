package fr.webskills.academy.service;

import fr.webskills.academy.domain.*;
import fr.webskills.academy.domain.enums.Role;
import fr.webskills.academy.dto.AuthDtos.*;
import fr.webskills.academy.exception.*;
import fr.webskills.academy.repository.*;
import fr.webskills.academy.security.*;
import java.time.Instant;
import java.util.UUID;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final AccessCodeRepository codeRepo;
    private final UserRepository userRepo;
    private final PasswordEncoder encoder;
    private final JwtService jwt;
    private final AuthenticationManager authManager;

    public AuthService(
            AccessCodeRepository codeRepo,
            UserRepository userRepo,
            PasswordEncoder encoder,
            JwtService jwt,
            AuthenticationManager authManager) {
        this.codeRepo = codeRepo;
        this.userRepo = userRepo;
        this.encoder = encoder;
        this.jwt = jwt;
        this.authManager = authManager;
    }

    @Transactional
    public AuthResponse authenticateWithCode(String rawCode) {
        AccessCode code =
                codeRepo.findAll().stream()
                        .filter(c -> encoder.matches(rawCode, c.getCodeHash()))
                        .findFirst()
                        .orElseThrow(
                                () ->
                                        new InvalidAccessCodeException(
                                                "Le code d’accès est invalide"));
        if (!code.isActive())
            throw new DisabledAccessCodeException("Le code d’accès est désactivé");
        if (code.getExpiresAt() != null && code.getExpiresAt().isBefore(Instant.now()))
            throw new ExpiredAccessCodeException("Le code d’accès est expiré");
        if (code.getMaxUses() != null && code.getUsageCount() >= code.getMaxUses())
            throw new AccessCodeUsageLimitReachedException(
                    "La limite d’utilisation du code est atteinte");
        code.setUsageCount(code.getUsageCount() + 1);
        code.setLastUsedAt(Instant.now());
        User user = new User();
        user.setEmail("learner-" + UUID.randomUUID() + "@access.local");
        user.setFirstName("Apprenant");
        user.setLastName(code.getLabel());
        user.setRole(Role.LEARNER);
        user.setEnabled(true);
        userRepo.save(user);
        return new AuthResponse(jwt.generate(user), "Bearer", user.getRole(), user.getId());
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        User user =
                userRepo.findByEmail(request.email())
                        .orElseThrow(() -> new UnauthorizedException("Identifiants invalides"));
        if (!user.isEnabled() || user.getRole() != Role.ADMIN)
            throw new ForbiddenException("Accès administrateur requis");
        return new AuthResponse(jwt.generate(user), "Bearer", user.getRole(), user.getId());
    }

    public MeResponse me(AcademyUserDetails details) {
        User u = details.user();
        return new MeResponse(
                u.getId(), u.getEmail(), u.getFirstName(), u.getLastName(), u.getRole());
    }
}
