package com.ecom.proj.controller;

import com.ecom.proj.dto.interaction.InteractionDtos.*;
import com.ecom.proj.security.CurrentUser;
import com.ecom.proj.service.OfferService;
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
@PreAuthorize("isAuthenticated()")
@Tag(name = "Offers")
public class OfferController {

    private final OfferService offerService;

    @PostMapping("/vehicles/{vehicleId}/offers")
    public ResponseEntity<OfferDto> create(@PathVariable Long vehicleId,
            @Valid @RequestBody NewOfferRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(offerService.create(vehicleId, CurrentUser.id(), req));
    }

    @GetMapping("/offers/received")
    public ResponseEntity<Page<OfferDto>> received(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(offerService.asSeller(CurrentUser.id(), page, size));
    }

    @GetMapping("/offers/sent")
    public ResponseEntity<Page<OfferDto>> sent(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(offerService.asBuyer(CurrentUser.id(), page, size));
    }

    @PatchMapping("/offers/{id}/accept")
    public ResponseEntity<OfferDto> accept(@PathVariable Long id) {
        return ResponseEntity.ok(offerService.accept(id, CurrentUser.id()));
    }

    @PatchMapping("/offers/{id}/reject")
    public ResponseEntity<OfferDto> reject(@PathVariable Long id) {
        return ResponseEntity.ok(offerService.reject(id, CurrentUser.id()));
    }

    @PatchMapping("/offers/{id}/withdraw")
    public ResponseEntity<OfferDto> withdraw(@PathVariable Long id) {
        return ResponseEntity.ok(offerService.withdraw(id, CurrentUser.id()));
    }
}
