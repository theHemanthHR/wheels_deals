package com.ecom.proj.service;

import com.ecom.proj.dto.admin.AdminDtos.*;
import com.ecom.proj.entity.*;
import com.ecom.proj.exception.ApiException;
import com.ecom.proj.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;
    private final ReportRepository reportRepository;
    private final AuditLogRepository auditLogRepository;

    private void audit(User admin, String action, String targetType, Long targetId, String details) {
        auditLogRepository.save(AuditLog.builder().admin(admin).action(action)
                .targetType(targetType).targetId(targetId).details(details).build());
    }

    @Transactional(readOnly = true)
    public Page<AdminUserDto> listUsers(String q, int page, int size) {
        Pageable p = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100), Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<User> res = (q == null || q.isBlank()) ? userRepository.findAll(p)
                : userRepository.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(q, q, p);
        return res.map(u -> new AdminUserDto(u.getId(), u.getName(), u.getEmail(), u.getPhone(),
                u.getCity(), u.getState(), u.getRole(), u.isEnabled(), u.getCreatedAt()));
    }

    @Transactional
    public AdminUserDto setEnabled(Long adminId, Long userId, boolean enabled) {
        User admin = userRepository.findById(adminId).orElseThrow(() -> ApiException.notFound("Admin not found"));
        User target = userRepository.findById(userId).orElseThrow(() -> ApiException.notFound("User not found"));
        if (target.getRole() == Role.ADMIN && !enabled)
            throw ApiException.badRequest("Admins cannot be disabled via this endpoint");
        target.setEnabled(enabled);
        audit(admin, enabled ? "USER_ENABLE" : "USER_DISABLE", "USER", userId, null);
        return new AdminUserDto(target.getId(), target.getName(), target.getEmail(), target.getPhone(),
                target.getCity(), target.getState(), target.getRole(), target.isEnabled(), target.getCreatedAt());
    }

    @Transactional(readOnly = true)
    public Page<AdminVehicleDto> listVehicles(String q, int page, int size) {
        Pageable p = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100), Sort.by(Sort.Direction.DESC, "createdAt"));
        return vehicleRepository.findAll(p).map(v -> new AdminVehicleDto(v.getId(), v.getCompany(), v.getModel(), v.getYear(),
                v.getSeller().getName(), v.getSeller().getEmail(),
                v.getStatus().name(), v.getRemovedReason(), v.getCreatedAt()));
    }

    @Transactional
    public void removeVehicle(Long adminId, Long vehicleId, String reason) {
        User admin = userRepository.findById(adminId).orElseThrow(() -> ApiException.notFound("Admin not found"));
        Vehicle v = vehicleRepository.findById(vehicleId).orElseThrow(() -> ApiException.notFound("Vehicle not found"));
        v.setStatus(ListingStatus.REMOVED);
        v.setRemovedReason(reason == null || reason.isBlank() ? "Removed by admin" : reason.trim());
        audit(admin, "VEHICLE_REMOVE", "VEHICLE", vehicleId, v.getRemovedReason());
    }

    @Transactional(readOnly = true)
    public Page<AdminReportDto> listReports(ReportStatus status, int page, int size) {
        Pageable p = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100), Sort.by(Sort.Direction.DESC, "createdAt"));
        return (status == null ? reportRepository.findAll(p) : reportRepository.findByStatusOrderByCreatedAtDesc(status, p))
                .map(r -> new AdminReportDto(r.getId(), r.getVehicle().getId(),
                        r.getVehicle().getCompany() + " " + r.getVehicle().getModel(),
                        r.getReporter().getName(), r.getReason(), r.getStatus(),
                        r.getAdminNote(), r.getCreatedAt()));
    }

    @Transactional
    public AdminReportDto resolveReport(Long adminId, Long reportId, ResolveReportRequest req) {
        User admin = userRepository.findById(adminId).orElseThrow(() -> ApiException.notFound("Admin not found"));
        Report r = reportRepository.findById(reportId).orElseThrow(() -> ApiException.notFound("Report not found"));
        r.setStatus(req.status()); r.setAdminNote(req.adminNote());
        audit(admin, "REPORT_" + req.status().name(), "REPORT", reportId, req.adminNote());
        return new AdminReportDto(r.getId(), r.getVehicle().getId(),
                r.getVehicle().getCompany() + " " + r.getVehicle().getModel(),
                r.getReporter().getName(), r.getReason(), r.getStatus(), r.getAdminNote(), r.getCreatedAt());
    }

    @Transactional(readOnly = true)
    public Page<AuditLogDto> auditLogs(int page, int size) {
        Pageable p = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100));
        return auditLogRepository.findAllByOrderByCreatedAtDesc(p).map(a -> new AuditLogDto(
                a.getId(), a.getAdmin() == null ? null : a.getAdmin().getEmail(),
                a.getAction(), a.getTargetType(), a.getTargetId(), a.getDetails(), a.getCreatedAt()));
    }
}
