package com.ecom.proj.config;

import com.ecom.proj.entity.AuthProvider;
import com.ecom.proj.entity.Role;
import com.ecom.proj.entity.User;
import com.ecom.proj.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.admin.email:admin@ecom.local}")
    private String adminEmail;

    @Value("${app.seed.admin.password:Admin@12345}")
    private String adminPassword;

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.existsByEmailIgnoreCase(adminEmail)) {
            log.info("Admin already exists: {}", adminEmail);
            return;
        }
        User admin = User.builder()
                .name("Platform Admin")
                .email(adminEmail.toLowerCase())
                .password(passwordEncoder.encode(adminPassword))
                .role(Role.ADMIN)
                .authProvider(AuthProvider.LOCAL)
                .enabled(true)
                .showPhone(false)
                .showEmail(false)
                .build();
        userRepository.save(admin);
        log.info("Seeded admin account: {}", adminEmail);
        log.warn("Default admin password: {} — CHANGE IT IMMEDIATELY", adminPassword);
    }
}
