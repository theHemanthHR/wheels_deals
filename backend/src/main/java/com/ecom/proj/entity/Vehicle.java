package com.ecom.proj.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "vehicles", indexes = {
        @Index(name = "idx_veh_status", columnList = "status"),
        @Index(name = "idx_veh_category", columnList = "category"),
        @Index(name = "idx_veh_company", columnList = "company"),
        @Index(name = "idx_veh_location", columnList = "location"),
        @Index(name = "idx_veh_seller", columnList = "seller_id")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Vehicle {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VehicleCategory category;

    @Column(nullable = false, length = 80)
    private String company;

    @Column(nullable = false, length = 80)
    private String model;

    @Column(name = "manufacture_year", nullable = false)
    private Integer year;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer mileage;

    @Column(nullable = false, length = 160)
    private String location;

    @Column(nullable = false, length = 4000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ListingStatus status = ListingStatus.ACTIVE;

    @Column(name = "removed_reason", length = 500)
    private String removedReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @OneToMany(mappedBy = "vehicle", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC")
    @Builder.Default
    private List<VehicleImage> images = new ArrayList<>();

    @PrePersist
    void onCreate() { this.createdAt = Instant.now(); this.updatedAt = this.createdAt; }

    @PreUpdate
    void onUpdate() { this.updatedAt = Instant.now(); }
}
