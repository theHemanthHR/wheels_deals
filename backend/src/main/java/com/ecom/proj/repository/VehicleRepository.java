package com.ecom.proj.repository;

import com.ecom.proj.entity.ListingStatus;
import com.ecom.proj.entity.Vehicle;
import com.ecom.proj.entity.VehicleCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    Page<Vehicle> findBySellerId(Long sellerId, Pageable pageable);
    Page<Vehicle> findByStatus(ListingStatus status, Pageable pageable);

    @Query("""
      SELECT v FROM Vehicle v
      WHERE v.status = :status
            AND (:category IS NULL OR v.category = :category)
            AND LOWER(v.company)   LIKE LOWER(CONCAT('%', :company,  '%'))
            AND LOWER(v.location)  LIKE LOWER(CONCAT('%', :location, '%'))
            AND (
                  LOWER(v.company)     LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                  LOWER(v.model)       LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                  LOWER(v.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                  LOWER(v.location)    LIKE LOWER(CONCAT('%', :keyword, '%'))
            )
      """)
      Page<Vehicle> search(@Param("status")   ListingStatus status,
                        @Param("category") VehicleCategory category,
                        @Param("company")  String company,
                        @Param("location") String location,
                        @Param("keyword")  String keyword,
                        Pageable pageable);
}
