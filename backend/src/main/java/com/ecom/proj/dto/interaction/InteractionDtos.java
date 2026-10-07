package com.ecom.proj.dto.interaction;

import com.ecom.proj.entity.OfferStatus;
import com.ecom.proj.entity.ReportStatus;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public final class InteractionDtos {
    private InteractionDtos() {}

    public record NewInquiryRequest(
            @NotBlank @Size(min = 2, max = 2000) String message, Long parentId) {}

    public record InquiryDto(Long id, Long vehicleId, String vehicleTitle,
                             Long buyerId, String buyerName, Long sellerId, String sellerName,
                             Long parentId, String message, Instant createdAt) {}

    public record InquiryThread(Long rootId, InquiryDto root, List<InquiryDto> replies) {}

    public record NewOfferRequest(
            @NotNull @DecimalMin("0.01") BigDecimal amount, @Size(max = 1000) String message) {}

    public record OfferDto(Long id, Long vehicleId, String vehicleTitle, BigDecimal vehiclePrice,
                           Long buyerId, String buyerName, Long sellerId, String sellerName,
                           BigDecimal amount, String message, OfferStatus status,
                           Instant createdAt, Instant updatedAt) {}

    public record OfferDecisionRequest(String note) {}

    public record WishlistItemDto(Long vehicleId, String company, String model, Integer year,
                                  BigDecimal price, String coverImageUrl, Instant addedAt) {}

    public record NewReviewRequest(@NotNull @Min(1) @Max(5) Integer rating,
                                   @Size(max = 1000) String comment) {}

    public record ReviewDto(Long id, Long vehicleId,
                            Long reviewerId, String reviewerName,
                            Long revieweeId, String revieweeName,
                            Integer rating, String comment, Instant createdAt) {}

    public record NewReportRequest(@NotBlank @Size(max = 500) String reason) {}

    public record ReportDto(Long id, Long vehicleId, String vehicleTitle,
                            Long reporterId, String reporterName,
                            String reason, ReportStatus status, String adminNote,
                            Instant createdAt) {}
}
