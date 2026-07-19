package fr.webskills.academy.service;

import fr.webskills.academy.domain.*;
import fr.webskills.academy.domain.enums.Role;
import fr.webskills.academy.dto.AuthDtos.*;
import fr.webskills.academy.exception.*;
import fr.webskills.academy.repository.*;
import fr.webskills.academy.security.*;
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
        validateAccessCode(rawCode);
        User user = new User();
        user.setEmail("learner-" + java.util.UUID.randomUUID() + "@access.local");
        user.setFirstName("Apprenant");
        user.setLastName("WebSkills");
        user.setRole(Role.LEARNER);
        user.setEnabled(true);
        userRepo.save(user);
        return new AuthResponse(jwt.generate(user), "Bearer", user.getRole(), user.getId());
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        validateAccessCode(request.accessCode());
        if (userRepo.findByEmail(request.email()).isPresent()) {
            throw new IllegalArgumentException("Un compte existe déjà avec cet email");
        }
        User user = new User();
        user.setEmail(request.email());
        user.setPassword(encoder.encode(request.password()));
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
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
        if (!user.isEnabled() || user.getRole() != Role.ADMIN) {
            throw new ForbiddenException("Accès administrateur requis");
        }
        return new AuthResponse(jwt.generate(user), "Bearer", user.getRole(), user.getId());
    }

    public MeResponse me(AcademyUserDetails details) {
        User u = details.user();
        return new MeResponse(
                u.getId(), u.getEmail(), u.getFirstName(), u.getLastName(), u.getRole());
    }

    private AccessCode validateAccessCode(String rawCode) {
        AccessCode code =
                codeRepo.findByCode(rawCode)
                        .orElseGet(
                                () ->
                                        codeRepo.findAll().stream()
                                                .filter(
                                                        c ->
                                                                c.getCodeHash() != null
                                                                        && encoder.matches(
                                                                                rawCode,
                                                                                c.getCodeHash()))
                                                .findFirst()
                                                .orElseThrow(
                                                        () ->
                                                                new InvalidAccessCodeException(
                                                                        "Le code d’accès est invalide")));
        if (!code.isUsable()) {
            throw new DisabledAccessCodeException("Le code d’accès est révoqué ou désactivé");
        }
        return code;
    }
}
