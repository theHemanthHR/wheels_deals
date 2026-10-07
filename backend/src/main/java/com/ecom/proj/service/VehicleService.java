package com.ecom.proj.service;

import com.ecom.proj.dto.vehicle.VehicleDtos.*;
import com.ecom.proj.entity.*;
import com.ecom.proj.exception.ApiException;
import com.ecom.proj.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final VehicleImageRepository imageRepository;
    private final UserRepository userRepository;
    private final WishlistRepository wishlistRepository;
    private final InquiryRepository inquiryRepository;
    private final ReviewRepository reviewRepository;
    private final FileStorageService storage;

   @Transactional
    public VehicleResponse create(Long sellerId, CreateRequest req, List<MultipartFile> images) {
        storage.validateBatch(images);
        User seller = userRepository.findById(sellerId)
                .orElseThrow(() -> ApiException.notFound("Seller not found"));

        Vehicle v = Vehicle.builder()
                .seller(seller)
                .category(req.category())
                .company(req.company().trim())
                .model(req.model().trim())
                .year(req.year())
                .price(req.price())
                .mileage(req.mileage())
                .location(req.location().trim())
                .description(req.description().trim())
                .status(ListingStatus.ACTIVE)
                .build();

        // Save to obtain the ID — the upload folder is named after it
        vehicleRepository.saveAndFlush(v);

        // ✅ Add images IN PLACE to the existing collection. Never replace the reference.
        for (int i = 0; i < images.size(); i++) {
            String url = storage.storeVehicleImage(v.getId(), images.get(i));
            v.getImages().add(VehicleImage.builder()
                    .vehicle(v)
                    .imageUrl(url)
                    .displayOrder(i)
                    .build());
        }

        // ✅ cascade = ALL on Vehicle.images persists these children on flush
        vehicleRepository.saveAndFlush(v);

        log.info("Vehicle {} created by user {}", v.getId(), sellerId);
        return toResponse(v, sellerId);
    }

    @Transactional
    public VehicleResponse update(Long id, Long requesterId, UpdateRequest req) {
        Vehicle v = mustOwn(id, requesterId);
        if (v.getStatus() == ListingStatus.SOLD) throw ApiException.badRequest("Sold listings cannot be edited");
        v.setCategory(req.category()); v.setCompany(req.company().trim()); v.setModel(req.model().trim());
        v.setYear(req.year()); v.setPrice(req.price()); v.setMileage(req.mileage());
        v.setLocation(req.location().trim()); v.setDescription(req.description().trim());
        return toResponse(v, requesterId);
    }

   @Transactional
public VehicleResponse replaceImages(Long id, Long requesterId, List<MultipartFile> images) {
    Vehicle v = mustOwn(id, requesterId);
    storage.validateBatch(images);

    // 1. Remove old files from disk
    storage.deleteVehicleFolder(id);

    // 2. Clear old image entities IN PLACE — orphanRemoval deletes them from DB
    v.getImages().clear();
    vehicleRepository.saveAndFlush(v);

    // 3. Add new image entities IN PLACE
    for (int i = 0; i < images.size(); i++) {
        String url = storage.storeVehicleImage(id, images.get(i));
        v.getImages().add(VehicleImage.builder()
                .vehicle(v)
                .imageUrl(url)
                .displayOrder(i)
                .build());
    }
    vehicleRepository.saveAndFlush(v);

    return toResponse(v, requesterId);
}

    @Transactional
    public VehicleResponse markSold(Long id, Long requesterId, MarkSoldRequest req) {
        Vehicle v = mustOwn(id, requesterId);
        v.setStatus(ListingStatus.SOLD);
        if (req != null && req.note() != null && !req.note().isBlank()) v.setRemovedReason(truncate(req.note(), 500));
        return toResponse(v, requesterId);
    }

    @Transactional
    public VehicleResponse reactivate(Long id, Long requesterId) {
        Vehicle v = mustOwn(id, requesterId);
        if (v.getStatus() == ListingStatus.REMOVED)
            throw ApiException.forbidden("This listing was removed by an admin and cannot be reactivated");
        v.setStatus(ListingStatus.ACTIVE); v.setRemovedReason(null);
        return toResponse(v, requesterId);
    }

    @Transactional
    public void deleteBySeller(Long id, Long requesterId) {
        Vehicle v = mustOwn(id, requesterId);
        v.setStatus(ListingStatus.REMOVED);
        v.setRemovedReason("Removed by seller");
    }

    @Transactional(readOnly = true)
    public VehicleResponse getById(Long id, Long viewerId) {
        Vehicle v = vehicleRepository.findById(id).orElseThrow(() -> ApiException.notFound("Vehicle not found"));
        if (v.getStatus() == ListingStatus.REMOVED && !isOwnerOrAdmin(viewerId, v))
            throw ApiException.notFound("Vehicle not found");
        return toResponse(v, viewerId);
    }

   @Transactional(readOnly = true)
    public Page<VehicleSummary> search(String keyword, VehicleCategory category, String company, String location,
                                    int page, int size, String sort) {
        Sort sortSpec = switch (sort == null ? "" : sort) {
            case "price_asc"   -> Sort.by(Sort.Direction.ASC,  "price");
            case "price_desc"  -> Sort.by(Sort.Direction.DESC, "price");
            case "year_desc"   -> Sort.by(Sort.Direction.DESC, "year");
            case "mileage_asc" -> Sort.by(Sort.Direction.ASC,  "mileage");
            default            -> Sort.by(Sort.Direction.DESC, "createdAt");
        };
        Pageable pageable = PageRequest.of(Math.max(page, 0), clampSize(size), sortSpec);

        // ✅ Never pass null to the query — empty string matches everything via LIKE '%%'
        return vehicleRepository.search(
                ListingStatus.ACTIVE,
                category,
                nz(company),
                nz(location),
                nz(keyword),
                pageable).map(this::toSummary);
    }

    /** null → "" so Hibernate can always type the parameter as text. */
    private static String nz(String s) { return s == null ? "" : s.trim(); }

    @Transactional(readOnly = true)
    public Page<VehicleSummary> myListings(Long sellerId, int page, int size) {
        Pageable p = PageRequest.of(Math.max(page, 0), clampSize(size), Sort.by(Sort.Direction.DESC, "createdAt"));
        return vehicleRepository.findBySellerId(sellerId, p).map(this::toSummary);
    }

    private Vehicle mustOwn(Long id, Long requesterId) {
        Vehicle v = vehicleRepository.findById(id).orElseThrow(() -> ApiException.notFound("Vehicle not found"));
        if (!v.getSeller().getId().equals(requesterId)) throw ApiException.forbidden("You do not own this listing");
        return v;
    }

    private boolean isOwnerOrAdmin(Long viewerId, Vehicle v) {
        if (viewerId == null) return false;
        if (v.getSeller().getId().equals(viewerId)) return true;
        return userRepository.findById(viewerId).map(u -> u.getRole() == Role.ADMIN).orElse(false);
    }

    private static String blankToNull(String s) { return (s == null || s.isBlank()) ? null : s.trim(); }
    private static int clampSize(int size) { return size < 1 ? 12 : Math.min(size, 50); }
    private static String truncate(String s, int max) { return s.length() <= max ? s : s.substring(0, max); }

    private VehicleSummary toSummary(Vehicle v) {
        String cover = v.getImages().isEmpty() ? null : v.getImages().get(0).getImageUrl();
        return new VehicleSummary(v.getId(), v.getCategory(), v.getCompany(), v.getModel(), v.getYear(),
                v.getPrice(), v.getMileage(), v.getLocation(), cover, v.getStatus(), v.getCreatedAt());
    }

    private VehicleResponse toResponse(Vehicle v, Long viewerId) {
        User seller = v.getSeller();
        ContactInfo contact = null;
        if (viewerId != null) {
            String phone = seller.isShowPhone() ? seller.getPhone() : null;
            String email = seller.isShowEmail() ? seller.getEmail() : null;
            if (phone != null || email != null) contact = new ContactInfo(phone, email);
        }
        Double avg = reviewRepository.averageRatingForUser(seller.getId());
        List<ImageDto> imgs = v.getImages().stream()
                .map(i -> new ImageDto(i.getId(), i.getImageUrl(), i.getDisplayOrder())).toList();
        boolean wishlisted = viewerId != null && wishlistRepository.existsByIdUserIdAndIdVehicleId(viewerId, v.getId());
        long inquiries = inquiryRepository.countByVehicleIdAndParentIsNull(v.getId());
        long wishlistCount = wishlistRepository.countByIdVehicleId(v.getId());
        SellerSummary sellerSummary = new SellerSummary(seller.getId(), seller.getName(), seller.getAvatarUrl(),
                seller.getCity(), seller.getState(), avg == null ? null : Math.round(avg * 10) / 10.0,
                reviewRepository.findByRevieweeIdOrderByCreatedAtDesc(seller.getId(), PageRequest.of(0, 1)).getTotalElements());

        return new VehicleResponse(v.getId(), v.getCategory(), v.getCompany(), v.getModel(), v.getYear(),
                v.getPrice(), v.getMileage(), v.getLocation(), v.getDescription(),
                v.getStatus(), v.getRemovedReason(), v.getCreatedAt(), v.getUpdatedAt(),
                sellerSummary, imgs, contact, inquiries, wishlistCount, wishlisted);
    }
}
