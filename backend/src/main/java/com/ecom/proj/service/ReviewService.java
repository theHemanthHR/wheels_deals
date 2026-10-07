package com.ecom.proj.service;

import com.ecom.proj.dto.interaction.InteractionDtos.*;
import com.ecom.proj.entity.*;
import com.ecom.proj.exception.ApiException;
import com.ecom.proj.repository.OfferRepository;
import com.ecom.proj.repository.ReviewRepository;
import com.ecom.proj.repository.UserRepository;
import com.ecom.proj.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final VehicleRepository vehicleRepository;
    private final UserRepository userRepository;
    private final OfferRepository offerRepository;

    @Transactional
    public ReviewDto create(Long vehicleId, Long reviewerId, NewReviewRequest req) {
        Vehicle v = vehicleRepository.findById(vehicleId).orElseThrow(() -> ApiException.notFound("Vehicle not found"));
        if (v.getSeller().getId().equals(reviewerId)) throw ApiException.badRequest("You cannot review your own listing");
        if (reviewRepository.existsByVehicleIdAndReviewerId(vehicleId, reviewerId))
            throw ApiException.conflict("You have already reviewed this vehicle");

        boolean hasAcceptedOffer = offerRepository.findByVehicleIdOrderByCreatedAtDesc(vehicleId, PageRequest.of(0, 100)).stream()
                .anyMatch(o -> o.getBuyer().getId().equals(reviewerId)
                        && (o.getStatus() == OfferStatus.ACCEPTED || o.getStatus() == OfferStatus.COMPLETED));
        if (!hasAcceptedOffer)
            throw ApiException.forbidden("You can only review a vehicle after your offer has been accepted");

        User reviewer = userRepository.findById(reviewerId).orElseThrow(() -> ApiException.notFound("Reviewer not found"));
        Review saved = reviewRepository.save(Review.builder()
                .vehicle(v).reviewer(reviewer).reviewee(v.getSeller())
                .rating(req.rating()).comment(req.comment()).build());
        return toDto(saved);
    }

    @Transactional(readOnly = true)
    public Page<ReviewDto> forUser(Long userId, int page, int size) {
        Pageable p = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50), Sort.by(Sort.Direction.DESC, "createdAt"));
        return reviewRepository.findByRevieweeIdOrderByCreatedAtDesc(userId, p).map(this::toDto);
    }

    @Transactional(readOnly = true)
    public Long sellerIdForVehicle(Long vehicleId) {
        return vehicleRepository.findById(vehicleId).orElseThrow(() -> ApiException.notFound("Vehicle not found"))
                .getSeller().getId();
    }

    private ReviewDto toDto(Review r) {
        return new ReviewDto(r.getId(), r.getVehicle().getId(),
                r.getReviewer().getId(), r.getReviewer().getName(),
                r.getReviewee().getId(), r.getReviewee().getName(),
                r.getRating(), r.getComment(), r.getCreatedAt());
    }
}
