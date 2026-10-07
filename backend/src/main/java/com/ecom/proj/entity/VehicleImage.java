package com.ecom.proj.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "vehicle_images")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class VehicleImage {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    @Column(name = "display_order", nullable = false)
    @Builder.Default
    private Integer displayOrder = 0;
}
