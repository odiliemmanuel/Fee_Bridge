package com.feebridge.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

/** Request/response payloads for authentication and school onboarding. */
public final class AuthDtos {

    private AuthDtos() {
    }

    public record RegisterSchoolRequest(
            @NotBlank String schoolName,
            String schoolCode,
            @Email String schoolEmail,
            String schoolPhone,
            String schoolAddress,
            boolean hasDay,
            boolean hasBoarding,
            @NotBlank String adminFirstName,
            @NotBlank String adminLastName,
            @NotBlank @Email String adminEmail,
            String adminPhone,
            @NotBlank @Size(min = 8, message = "Password must be at least 8 characters") String adminPassword
    ) {
    }

    public record LoginRequest(
            @NotBlank @Email String email,
            @NotBlank String password
    ) {
    }

    public record AuthResponse(
            String token,
            String tokenType,
            long expiresInMinutes,
            UserSummary user
    ) {
    }

    public record UserSummary(
            Long id,
            String email,
            String firstName,
            String lastName,
            Long schoolId,
            String schoolName,
            List<String> roles
    ) {
    }
}
