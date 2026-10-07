package com.ecom.proj.controller;

import com.ecom.proj.dto.interaction.InteractionDtos.WishlistItemDto;
import com.ecom.proj.security.CurrentUser;
import com.ecom.proj.service.WishlistService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/wishlist")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
@Tag(name = "Wishlist")
public class WishlistController {

    private final WishlistService wishlistService;

    @PostMapping("/{vehicleId}")
    public ResponseEntity<Void> add(@PathVariable Long vehicleId) {
        wishlistService.add(CurrentUser.id(), vehicleId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{vehicleId}")
    public ResponseEntity<Void> remove(@PathVariable Long vehicleId) {
        wishlistService.remove(CurrentUser.id(), vehicleId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<Page<WishlistItemDto>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        return ResponseEntity.ok(wishlistService.list(CurrentUser.id(), page, size));
    }
}
