package com.ecom.proj.service;

import com.ecom.proj.dto.interaction.InteractionDtos.*;
import com.ecom.proj.entity.*;
import com.ecom.proj.exception.ApiException;
import com.ecom.proj.repository.ReportRepository;
import com.ecom.proj.repository.UserRepository;
import com.ecom.proj.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final VehicleRepository vehicleRepository;
    private final UserRepository userRepository;

    @Transactional
    public ReportDto create(Long vehicleId, Long reporterId, NewReportRequest req) {
        Vehicle v = vehicleRepository.findById(vehicleId).orElseThrow(() -> ApiException.notFound("Vehicle not found"));
        if (v.getSeller().getId().equals(reporterId)) throw ApiException.badRequest("You cannot report your own listing");
        if (reportRepository.existsByVehicleIdAndReporterIdAndStatus(vehicleId, reporterId, ReportStatus.OPEN))
            throw ApiException.conflict("You already have an open report on this listing");
        User reporter = userRepository.findById(reporterId).orElseThrow(() -> ApiException.notFound("User not found"));
        Report saved = reportRepository.save(Report.builder()
                .vehicle(v).reporter(reporter).reason(req.reason().trim()).status(ReportStatus.OPEN).build());
        return toDto(saved);
    }

    private ReportDto toDto(Report r) {
        Vehicle v = r.getVehicle();
        return new ReportDto(r.getId(), v.getId(),
                v.getCompany() + " " + v.getModel() + " (" + v.getYear() + ")",
                r.getReporter().getId(), r.getReporter().getName(),
                r.getReason(), r.getStatus(), r.getAdminNote(), r.getCreatedAt());
    }
}
