package com.ecom.proj.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_users_email", columnList = "email", unique = true)
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class User {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, unique = true, length = 180)
    private String email;

    @Column(length = 100)
    private String password;

    @Column(length = 20)
    private String phone;

    @Column(length = 80)
    private String city;

    @Column(length = 80)
    private String state;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Column(length = 1000)
    private String bio;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private Role role = Role.USER;

    @Enumerated(EnumType.STRING)
    @Column(name = "auth_provider", nullable = false, length = 20)
    @Builder.Default
    private AuthProvider authProvider = AuthProvider.LOCAL;

    @Column(nullable = false)
    @Builder.Default
    private boolean enabled = true;

    @Column(name = "show_phone", nullable = false)
    @Builder.Default
    private boolean showPhone = true;

    @Column(name = "show_email", nullable = false)
    @Builder.Default
    private boolean showEmail = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    void onCreate() { this.createdAt = Instant.now(); this.updatedAt = this.createdAt; }

    @PreUpdate
    void onUpdate() { this.updatedAt = Instant.now(); }
}
