package com.ecom.proj.repository;

import com.ecom.proj.entity.Inquiry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface InquiryRepository extends JpaRepository<Inquiry, Long> {

    @Query("""
        SELECT i FROM Inquiry i
        WHERE i.vehicle.id = :vehicleId
          AND i.buyer.id = :buyerId
          AND i.parent IS NULL
        ORDER BY i.createdAt DESC
    """)
    List<Inquiry> findRootThreads(@Param("vehicleId") Long vehicleId, @Param("buyerId") Long buyerId);

    List<Inquiry> findByParentIdOrderByCreatedAtAsc(Long parentId);
    List<Inquiry> findByVehicleIdAndParentIsNullOrderByCreatedAtDesc(Long vehicleId);
    long countByVehicleIdAndParentIsNull(Long vehicleId);
}
