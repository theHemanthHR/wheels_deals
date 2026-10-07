package com.ecom.proj.service;

import com.ecom.proj.dto.auth.AuthDtos.*;
import com.ecom.proj.entity.AuthProvider;
import com.ecom.proj.entity.Role;
import com.ecom.proj.entity.User;
import com.ecom.proj.exception.ApiException;
import com.ecom.proj.repository.UserRepository;
import com.ecom.proj.security.JwtService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Value("${app.jwt.access-token-ttl-ms}")
    private long accessTtlMs;

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        String email = req.email().trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(email))
            throw ApiException.conflict("An account with this email already exists");

        User user = User.builder()
                .name(req.name().trim()).email(email)
                .password(passwordEncoder.encode(req.password()))
                .phone(req.phone()).city(req.city()).state(req.state())
                .role(Role.USER).authProvider(AuthProvider.LOCAL).enabled(true).build();
        userRepository.save(user);
        log.info("New user registered: {}", email);
        return issueTokens(user);
    }

    public AuthResponse login(LoginRequest req) {
        String email = req.email().trim().toLowerCase();
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));
        if (user.getAuthProvider() == AuthProvider.GOOGLE && user.getPassword() == null)
            throw new ApiException(HttpStatus.BAD_REQUEST, "This account uses Google Sign-In.");
        if (!user.isEnabled()) throw ApiException.forbidden("Account disabled.");
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, req.password()));
        return issueTokens(user);
    }

    public AuthResponse refresh(RefreshRequest req) {
        Claims claims;
        try { claims = jwtService.parse(req.refreshToken()); }
        catch (Exception e) { throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid or expired refresh token"); }
        if (!"refresh".equals(claims.get("typ", String.class)))
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Not a refresh token");

        User user = userRepository.findByEmailIgnoreCase(claims.getSubject())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "User no longer exists"));
        if (!user.isEnabled()) throw ApiException.forbidden("Account disabled");
        return issueTokens(user);
    }

    /** SECURITY-RISK — NO VERIFICATION. Per project spec. */
    @Transactional
    public void resetPassword(ResetPasswordRequest req) {
        User user = userRepository.findByEmailIgnoreCase(req.email().trim().toLowerCase())
                .orElseThrow(() -> ApiException.notFound("No account found with this email"));
        if (user.getAuthProvider() == AuthProvider.GOOGLE)
            throw ApiException.badRequest("This account uses Google Sign-In and has no password.");
        user.setPassword(passwordEncoder.encode(req.newPassword()));
        userRepository.save(user);
        log.warn("SECURITY: Password reset without verification for {}", user.getEmail());
    }

    private AuthResponse issueTokens(User user) {
        return new AuthResponse(
                jwtService.generateAccessToken(user.getEmail(), user.getRole().name(), user.getId()),
                jwtService.generateRefreshToken(user.getEmail()),
                "Bearer", accessTtlMs,
                new UserSummary(user.getId(), user.getName(), user.getEmail(),
                        user.getRole().name(), user.getAvatarUrl(), user.getCity(), user.getState()));
    }
}
