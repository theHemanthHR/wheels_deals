package com.ecom.proj.repository;

import com.ecom.proj.entity.Wishlist;
import com.ecom.proj.entity.WishlistId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WishlistRepository extends JpaRepository<Wishlist, WishlistId> {
    Page<Wishlist> findByIdUserId(Long userId, Pageable pageable);
    boolean existsByIdUserIdAndIdVehicleId(Long userId, Long vehicleId);
    void deleteByIdUserIdAndIdVehicleId(Long userId, Long vehicleId);
    long countByIdVehicleId(Long vehicleId);
}
