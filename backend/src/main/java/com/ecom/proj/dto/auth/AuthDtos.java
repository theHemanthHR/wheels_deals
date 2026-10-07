package com.ecom.proj.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class AuthDtos {
    private AuthDtos() {}

    public record RegisterRequest(
            @NotBlank @Size(min = 2, max = 120) String name,
            @NotBlank @Email String email,
            @NotBlank @Size(min = 8, max = 72) String password,
            @Size(max = 20) String phone,
            @Size(max = 80) String city,
            @Size(max = 80) String state) {}

    public record LoginRequest(@NotBlank @Email String email, @NotBlank String password) {}
    public record RefreshRequest(@NotBlank String refreshToken) {}

    /** SECURITY-RISK: NO ownership verification. Add email/OTP before public use. */
    public record ResetPasswordRequest(
            @NotBlank @Email String email,
            @NotBlank @Size(min = 8, max = 72) String newPassword) {}

    public record AuthResponse(String accessToken, String refreshToken, String tokenType,
                               long expiresInMs, UserSummary user) {}

    public record UserSummary(Long id, String name, String email, String role,
                              String avatarUrl, String city, String state) {}
}
