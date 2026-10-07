package com.ecom.proj.repository;

import com.ecom.proj.entity.Offer;
import com.ecom.proj.entity.OfferStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OfferRepository extends JpaRepository<Offer, Long> {
    Page<Offer> findBySellerIdOrderByCreatedAtDesc(Long sellerId, Pageable pageable);
    Page<Offer> findByBuyerIdOrderByCreatedAtDesc(Long buyerId, Pageable pageable);
    Page<Offer> findByVehicleIdOrderByCreatedAtDesc(Long vehicleId, Pageable pageable);
    boolean existsByVehicleIdAndBuyerIdAndStatus(Long vehicleId, Long buyerId, OfferStatus status);
}
