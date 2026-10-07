package com.ecom.proj.controller;

import com.ecom.proj.dto.vehicle.VehicleDtos.*;
import com.ecom.proj.entity.VehicleCategory;
import com.ecom.proj.security.CurrentUser;
import com.ecom.proj.service.VehicleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@RequestMapping("/api/v1/vehicles")
@RequiredArgsConstructor
@Tag(name = "Vehicles")
public class VehicleController {

    private final VehicleService vehicleService;

    @GetMapping
    public ResponseEntity<Page<VehicleSummary>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) VehicleCategory category,
            @RequestParam(required = false) String company,
            @RequestParam(required = false) String location,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "newest") String sort) {
        return ResponseEntity.ok(vehicleService.search(keyword, category, company, location, page, size, sort));
    }

    @GetMapping("/{id}")
    public ResponseEntity<VehicleResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(vehicleService.getById(id, tryCurrentUserId()));
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<VehicleSummary>> myListings(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "12") int size) {
        return ResponseEntity.ok(vehicleService.myListings(CurrentUser.id(), page, size));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<VehicleResponse> create(
            @RequestPart("data")  @Valid CreateRequest data,
            @RequestPart("images") List<MultipartFile> images) {
        return ResponseEntity.status(HttpStatus.CREATED).body(vehicleService.create(CurrentUser.id(), data, images));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<VehicleResponse> update(@PathVariable Long id, @Valid @RequestBody UpdateRequest req) {
        return ResponseEntity.ok(vehicleService.update(id, CurrentUser.id(), req));
    }

    @PutMapping(value = "/{id}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<VehicleResponse> replaceImages(
            @PathVariable Long id,
            @RequestPart("images") List<MultipartFile> images) {
        return ResponseEntity.ok(vehicleService.replaceImages(id, CurrentUser.id(), images));
    }

    @PatchMapping("/{id}/sold")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<VehicleResponse> markSold(@PathVariable Long id,
            @RequestBody(required = false) MarkSoldRequest req) {
        return ResponseEntity.ok(vehicleService.markSold(id, CurrentUser.id(), req));
    }

    @PatchMapping("/{id}/reactivate")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<VehicleResponse> reactivate(@PathVariable Long id) {
        return ResponseEntity.ok(vehicleService.reactivate(id, CurrentUser.id()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        vehicleService.deleteBySeller(id, CurrentUser.id());
        return ResponseEntity.noContent().build();
    }

    private static Long tryCurrentUserId() {
        try { return CurrentUser.id(); } catch (Exception e) { return null; }
    }
}
