package com.ecom.proj.dto.admin;

import com.ecom.proj.entity.ReportStatus;
import com.ecom.proj.entity.Role;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public final class AdminDtos {
    private AdminDtos() {}

    public record AdminUserDto(Long id, String name, String email, String phone,
                               String city, String state, Role role, boolean enabled,
                               Instant createdAt) {}

    public record AdminVehicleDto(Long id, String company, String model, Integer year,
                                  String sellerName, String sellerEmail, String status,
                                  String removedReason, Instant createdAt) {}

    public record AdminReportDto(Long id, Long vehicleId, String vehicleTitle,
                                 String reporterName, String reason, ReportStatus status,
                                 String adminNote, Instant createdAt) {}

    public record AuditLogDto(Long id, String adminEmail, String action,
                              String targetType, Long targetId, String details,
                              Instant createdAt) {}

    public record SetEnabledRequest(@NotNull Boolean enabled) {}
    public record RemoveVehicleRequest(@Size(max = 500) String reason) {}
    public record ResolveReportRequest(@NotNull ReportStatus status, @Size(max = 500) String adminNote) {}
}
