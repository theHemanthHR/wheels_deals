package com.ecom.proj.service;

import com.ecom.proj.dto.interaction.InteractionDtos.*;
import com.ecom.proj.entity.*;
import com.ecom.proj.exception.ApiException;
import com.ecom.proj.repository.InquiryRepository;
import com.ecom.proj.repository.UserRepository;
import com.ecom.proj.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InquiryService {

    private final InquiryRepository inquiryRepository;
    private final VehicleRepository vehicleRepository;
    private final UserRepository userRepository;

    @Transactional
    public InquiryDto post(Long vehicleId, Long requesterId, NewInquiryRequest req) {
        Vehicle v = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> ApiException.notFound("Vehicle not found"));
        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> ApiException.notFound("User not found"));

        // ---------- NEW THREAD ----------
        if (req.parentId() == null) {
            if (v.getSeller().getId().equals(requesterId)) {
                throw ApiException.badRequest("Sellers cannot start an inquiry on their own listing");
            }
            if (v.getStatus() != ListingStatus.ACTIVE) {
                throw ApiException.badRequest("This listing is no longer accepting questions");
            }

            Inquiry saved = inquiryRepository.save(Inquiry.builder()
                    .vehicle(v)
                    .buyer(requester)
                    .seller(v.getSeller())
                    .parent(null)
                    .message(req.message().trim())
                    .build());
            return toDto(saved);
        }

        // ---------- REPLY ----------
        Inquiry parent = inquiryRepository.findById(req.parentId())
                .orElseThrow(() -> ApiException.notFound("Parent inquiry not found"));

        if (!parent.getVehicle().getId().equals(vehicleId)) {
            throw ApiException.badRequest("Parent inquiry belongs to a different listing");
        }

        // Walk up to the root inquiry
        Inquiry root = parent;
        while (root.getParent() != null) {
            root = root.getParent();
        }

        boolean isBuyer  = root.getBuyer().getId().equals(requesterId);
        boolean isSeller = root.getSeller().getId().equals(requesterId);
        if (!isBuyer && !isSeller) {
            throw ApiException.forbidden("You are not part of this thread");
        }

        // Active-listing check applies only to the buyer replying.
        // Sellers may still reply after marking the listing SOLD.
        if (isBuyer && v.getStatus() != ListingStatus.ACTIVE) {
            throw ApiException.badRequest("This listing is no longer accepting questions");
        }

        Inquiry reply = inquiryRepository.save(Inquiry.builder()
                .vehicle(v)
                .buyer(root.getBuyer())      // thread owner stays the buyer
                .seller(v.getSeller())
                .parent(root)                // always attach to the root, keeps threads 1-level
                .message(req.message().trim())
                .build());
        return toDto(reply);
}

    @Transactional(readOnly = true)
    public List<InquiryThread> getBuyerThreads(Long vehicleId, Long buyerId) {
        return inquiryRepository.findRootThreads(vehicleId, buyerId).stream().map(this::toThread).toList();
    }

    @Transactional(readOnly = true)
    public List<InquiryThread> getSellerThreads(Long vehicleId, Long sellerId) {
        Vehicle v = vehicleRepository.findById(vehicleId).orElseThrow(() -> ApiException.notFound("Vehicle not found"));
        if (!v.getSeller().getId().equals(sellerId)) throw ApiException.forbidden("You do not own this listing");
        return inquiryRepository.findByVehicleIdAndParentIsNullOrderByCreatedAtDesc(vehicleId)
                .stream().map(this::toThread).toList();
    }

    private InquiryThread toThread(Inquiry root) {
        List<InquiryDto> replies = inquiryRepository.findByParentIdOrderByCreatedAtAsc(root.getId())
                .stream().map(this::toDto).toList();
        return new InquiryThread(root.getId(), toDto(root), replies);
    }

    private InquiryDto toDto(Inquiry i) {
        Vehicle v = i.getVehicle();
        return new InquiryDto(i.getId(), v.getId(),
                v.getCompany() + " " + v.getModel() + " (" + v.getYear() + ")",
                i.getBuyer().getId(), i.getBuyer().getName(),
                i.getSeller().getId(), i.getSeller().getName(),
                i.getParent() == null ? null : i.getParent().getId(),
                i.getMessage(), i.getCreatedAt());
    }
}
