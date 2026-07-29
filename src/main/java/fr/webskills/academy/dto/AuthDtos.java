package fr.webskills.academy.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import fr.webskills.academy.domain.enums.Role;
import jakarta.validation.constraints.*;
import java.time.Instant;
import java.util.UUID;

public final class AuthDtos {
    private AuthDtos() {}

    public record AccessCodeLoginRequest(@NotBlank @Size(max = 120) String code) {}

    public record RegisterRequest(
            @NotBlank @Email String email,
            @NotBlank @Size(min = 8, max = 120) String password,
            @NotBlank @Size(max = 100) String firstName,
            @NotBlank @Size(max = 100) String lastName,
            @NotBlank @Size(max = 120) String accessCode) {}

    public record LoginRequest(
            @NotBlank @Email String email, @NotBlank @Size(min = 8, max = 120) String password) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record AuthResponse(
            String accessToken,
            String tokenType,
            Role role,
            UUID userId,
            UUID accessCodeId,
            String accessCodeLabel,
            String welcomeMessage,
            String recommendedPath,
            Instant startsAt,
            Instant expiresAt) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record MeResponse(
            UUID id,
            String email,
            String firstName,
            String lastName,
            Role role,
            UUID accessCodeId,
            String accessCodeLabel,
            String welcomeMessage,
            String recommendedPath,
            Instant startsAt,
            Instant expiresAt) {}
}
