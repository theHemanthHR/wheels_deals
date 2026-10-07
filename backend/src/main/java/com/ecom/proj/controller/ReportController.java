package com.ecom.proj.controller;

import com.ecom.proj.dto.interaction.InteractionDtos.*;
import com.ecom.proj.security.CurrentUser;
import com.ecom.proj.service.ReportService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/vehicles/{vehicleId}/reports")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
@Tag(name = "Reports")
public class ReportController {

    private final ReportService reportService;

    @PostMapping
    public ResponseEntity<ReportDto> create(@PathVariable Long vehicleId,
            @Valid @RequestBody NewReportRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reportService.create(vehicleId, CurrentUser.id(), req));
    }
}
