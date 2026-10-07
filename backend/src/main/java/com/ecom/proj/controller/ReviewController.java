package com.ecom.proj.controller;

import com.ecom.proj.dto.interaction.InteractionDtos.*;
import com.ecom.proj.security.CurrentUser;
import com.ecom.proj.service.ReviewService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/vehicles/{vehicleId}/reviews")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ReviewDto> create(@PathVariable Long vehicleId,
            @Valid @RequestBody NewReviewRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reviewService.create(vehicleId, CurrentUser.id(), req));
    }

    @GetMapping("/vehicles/{vehicleId}/reviews")
    public ResponseEntity<Page<ReviewDto>> forVehicleSeller(@PathVariable Long vehicleId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(reviewService.forUser(reviewService.sellerIdForVehicle(vehicleId), page, size));
    }

    @GetMapping("/users/{userId}/reviews")
    public ResponseEntity<Page<ReviewDto>> forUser(@PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(reviewService.forUser(userId, page, size));
    }
}
