package com.ecom.proj.repository;

import com.ecom.proj.entity.Report;
import com.ecom.proj.entity.ReportStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, Long> {
    Page<Report> findByStatusOrderByCreatedAtDesc(ReportStatus status, Pageable pageable);
    boolean existsByVehicleIdAndReporterIdAndStatus(Long vehicleId, Long reporterId, ReportStatus status);
}
