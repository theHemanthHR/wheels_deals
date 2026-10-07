package com.ecom.proj.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "offers", indexes = {
        @Index(name = "idx_offer_vehicle", columnList = "vehicle_id"),
        @Index(name = "idx_offer_buyer", columnList = "buyer_id"),
        @Index(name = "idx_offer_seller", columnList = "seller_id")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Offer {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "buyer_id", nullable = false)
    private User buyer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(length = 1000)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private OfferStatus status = OfferStatus.PENDING;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    void onCreate() { this.createdAt = Instant.now(); this.updatedAt = this.createdAt; }

    @PreUpdate
    void onUpdate() { this.updatedAt = Instant.now(); }
}
