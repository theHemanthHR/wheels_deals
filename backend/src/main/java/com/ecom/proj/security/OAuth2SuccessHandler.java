package com.ecom.proj.security;

import com.ecom.proj.entity.AuthProvider;
import com.ecom.proj.entity.Role;
import com.ecom.proj.entity.User;
import com.ecom.proj.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final JwtService jwtService;

    @Value("${app.oauth2.redirect-uri}")
    private String redirectUri;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String picture = oAuth2User.getAttribute("picture");

        if (email == null) { response.sendRedirect(redirectUri + "#error=email_not_returned"); return; }

        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseGet(() -> userRepository.save(User.builder()
                        .email(email.toLowerCase())
                        .name(name == null ? email : name)
                        .avatarUrl(picture)
                        .role(Role.USER)
                        .authProvider(AuthProvider.GOOGLE)
                        .enabled(true).build()));

        if (!user.isEnabled()) { response.sendRedirect(redirectUri + "#error=account_disabled"); return; }

        String access = jwtService.generateAccessToken(user.getEmail(), user.getRole().name(), user.getId());
        String refresh = jwtService.generateRefreshToken(user.getEmail());

        String url = UriComponentsBuilder.fromUriString(redirectUri)
                .fragment("accessToken=" + enc(access) + "&refreshToken=" + enc(refresh))
                .build(true).toUriString();
        response.sendRedirect(url);
    }

    private static String enc(String s) { return URLEncoder.encode(s, StandardCharsets.UTF_8); }
}
