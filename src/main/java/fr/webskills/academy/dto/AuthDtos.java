package fr.webskills.academy.dto;

import fr.webskills.academy.domain.enums.Role;
import jakarta.validation.constraints.*;
import java.util.UUID;

public final class AuthDtos {
    private AuthDtos() {}

    public record AccessCodeLoginRequest(@NotBlank @Size(max = 120) String code) {}

    public record LoginRequest(
            @NotBlank @Email String email, @NotBlank @Size(min = 8, max = 120) String password) {}

    public record AuthResponse(String accessToken, String tokenType, Role role, UUID userId) {}

    public record MeResponse(UUID id, String email, String firstName, String lastName, Role role) {}
}
