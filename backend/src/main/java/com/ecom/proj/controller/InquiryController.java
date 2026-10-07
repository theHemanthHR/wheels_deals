package com.ecom.proj.controller;

import com.ecom.proj.dto.interaction.InteractionDtos.*;
import com.ecom.proj.security.CurrentUser;
import com.ecom.proj.service.InquiryService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/vehicles/{vehicleId}/inquiries")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
@Tag(name = "Inquiries")
public class InquiryController {

    private final InquiryService inquiryService;

    @PostMapping
    public ResponseEntity<InquiryDto> post(@PathVariable Long vehicleId,
            @Valid @RequestBody NewInquiryRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(inquiryService.post(vehicleId, CurrentUser.id(), req));
    }

    @GetMapping("/mine")
    public ResponseEntity<List<InquiryThread>> mine(@PathVariable Long vehicleId) {
        return ResponseEntity.ok(inquiryService.getBuyerThreads(vehicleId, CurrentUser.id()));
    }

    @GetMapping("/as-seller")
    public ResponseEntity<List<InquiryThread>> asSeller(@PathVariable Long vehicleId) {
        return ResponseEntity.ok(inquiryService.getSellerThreads(vehicleId, CurrentUser.id()));
    }
}
