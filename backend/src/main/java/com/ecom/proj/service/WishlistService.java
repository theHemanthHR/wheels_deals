package com.ecom.proj.service;

import com.ecom.proj.dto.interaction.InteractionDtos.WishlistItemDto;
import com.ecom.proj.entity.*;
import com.ecom.proj.exception.ApiException;
import com.ecom.proj.repository.UserRepository;
import com.ecom.proj.repository.VehicleRepository;
import com.ecom.proj.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final VehicleRepository vehicleRepository;
    private final UserRepository userRepository;

    @Transactional
    public void add(Long userId, Long vehicleId) {
        if (wishlistRepository.existsByIdUserIdAndIdVehicleId(userId, vehicleId)) return;
        User u = userRepository.getReferenceById(userId);
        Vehicle v = vehicleRepository.findById(vehicleId).orElseThrow(() -> ApiException.notFound("Vehicle not found"));
        wishlistRepository.save(Wishlist.builder().id(new WishlistId(userId, vehicleId)).user(u).vehicle(v).build());
    }

    @Transactional
    public void remove(Long userId, Long vehicleId) {
        wishlistRepository.deleteByIdUserIdAndIdVehicleId(userId, vehicleId);
    }

    @Transactional(readOnly = true)
    public Page<WishlistItemDto> list(Long userId, int page, int size) {
        Pageable p = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50), Sort.by(Sort.Direction.DESC, "createdAt"));
        return wishlistRepository.findByIdUserId(userId, p).map(w -> {
            Vehicle v = w.getVehicle();
            String cover = v.getImages().isEmpty() ? null : v.getImages().get(0).getImageUrl();
            return new WishlistItemDto(v.getId(), v.getCompany(), v.getModel(), v.getYear(),
                    v.getPrice(), cover, w.getCreatedAt());
        });
    }
}
