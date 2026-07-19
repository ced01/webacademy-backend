package fr.webskills.academy.controller;

import fr.webskills.academy.dto.AuthDtos.*;
import fr.webskills.academy.security.AcademyUserDetails;
import fr.webskills.academy.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService auth;

    public AuthController(AuthService auth) {
        this.auth = auth;
    }

    @PostMapping("/access-code")
    AuthResponse accessCode(@RequestBody @Valid AccessCodeLoginRequest request) {
        return auth.authenticateWithCode(request.code());
    }

    @PostMapping("/register")
    AuthResponse register(@RequestBody @Valid RegisterRequest request) {
        return auth.register(request);
    }

    @PostMapping("/login")
    AuthResponse login(@RequestBody @Valid LoginRequest request) {
        return auth.login(request);
    }

    @GetMapping("/me")
    MeResponse me(@AuthenticationPrincipal AcademyUserDetails details) {
        return auth.me(details);
    }
}
