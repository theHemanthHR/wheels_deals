package com.ecom.proj.dto.vehicle;

import com.ecom.proj.entity.ListingStatus;
import com.ecom.proj.entity.VehicleCategory;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public final class VehicleDtos {
    private VehicleDtos() {}

    public record CreateRequest(
            @NotNull VehicleCategory category,
            @NotBlank @Size(max = 80)  String company,
            @NotBlank @Size(max = 80)  String model,
            @NotNull @Min(1900) @Max(2100) Integer year,
            @NotNull @DecimalMin("0.0") BigDecimal price,
            @NotNull @Min(0) Integer mileage,
            @NotBlank @Size(max = 160) String location,
            @NotBlank @Size(min = 20, max = 4000) String description) {}

    public record UpdateRequest(
            @NotNull VehicleCategory category,
            @NotBlank @Size(max = 80)  String company,
            @NotBlank @Size(max = 80)  String model,
            @NotNull @Min(1900) @Max(2100) Integer year,
            @NotNull @DecimalMin("0.0") BigDecimal price,
            @NotNull @Min(0) Integer mileage,
            @NotBlank @Size(max = 160) String location,
            @NotBlank @Size(min = 20, max = 4000) String description) {}

    public record ImageDto(Long id, String url, Integer displayOrder) {}
    public record SellerSummary(Long id, String name, String avatarUrl, String city, String state,
                                Double averageRating, Long reviewCount) {}
    public record ContactInfo(String phone, String email) {}

    public record VehicleResponse(
            Long id, VehicleCategory category, String company, String model,
            Integer year, BigDecimal price, Integer mileage, String location, String description,
            ListingStatus status, String removedReason, Instant createdAt, Instant updatedAt,
            SellerSummary seller, List<ImageDto> images, ContactInfo contact,
            long inquiryCount, long wishlistCount, boolean wishlistedByMe) {}

    public record VehicleSummary(
            Long id, VehicleCategory category, String company, String model, Integer year,
            BigDecimal price, Integer mileage, String location, String coverImageUrl,
            ListingStatus status, Instant createdAt) {}

    public record MarkSoldRequest(String note) {}
}
