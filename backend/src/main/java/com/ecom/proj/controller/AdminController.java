package com.ecom.proj.controller;

import com.ecom.proj.dto.admin.AdminDtos.*;
import com.ecom.proj.entity.ReportStatus;
import com.ecom.proj.security.CurrentUser;
import com.ecom.proj.service.AdminService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/users")
    public ResponseEntity<Page<AdminUserDto>> users(@RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(adminService.listUsers(q, page, size));
    }

    @PatchMapping("/users/{id}/enabled")
    public ResponseEntity<AdminUserDto> setEnabled(@PathVariable Long id,
            @Valid @RequestBody SetEnabledRequest req) {
        return ResponseEntity.ok(adminService.setEnabled(CurrentUser.id(), id, req.enabled()));
    }

    @GetMapping("/vehicles")
    public ResponseEntity<Page<AdminVehicleDto>> vehicles(@RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(adminService.listVehicles(q, page, size));
    }

    @DeleteMapping("/vehicles/{id}")
    public ResponseEntity<Void> removeVehicle(@PathVariable Long id,
            @RequestBody(required = false) RemoveVehicleRequest req) {
        adminService.removeVehicle(CurrentUser.id(), id, req == null ? null : req.reason());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/reports")
    public ResponseEntity<Page<AdminReportDto>> reports(@RequestParam(required = false) ReportStatus status,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(adminService.listReports(status, page, size));
    }

    @PatchMapping("/reports/{id}")
    public ResponseEntity<AdminReportDto> resolveReport(@PathVariable Long id,
            @Valid @RequestBody ResolveReportRequest req) {
        return ResponseEntity.ok(adminService.resolveReport(CurrentUser.id(), id, req));
    }

    @GetMapping("/audit-logs")
    public ResponseEntity<Page<AuditLogDto>> auditLogs(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(adminService.auditLogs(page, size));
    }
}
