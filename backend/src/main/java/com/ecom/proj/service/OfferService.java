package com.ecom.proj.service;

import com.ecom.proj.dto.interaction.InteractionDtos.*;
import com.ecom.proj.entity.*;
import com.ecom.proj.exception.ApiException;
import com.ecom.proj.repository.OfferRepository;
import com.ecom.proj.repository.UserRepository;
import com.ecom.proj.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OfferService {

    private final OfferRepository offerRepository;
    private final VehicleRepository vehicleRepository;
    private final UserRepository userRepository;

    @Transactional
    public OfferDto create(Long vehicleId, Long buyerId, NewOfferRequest req) {
        Vehicle v = vehicleRepository.findById(vehicleId).orElseThrow(() -> ApiException.notFound("Vehicle not found"));
        if (v.getSeller().getId().equals(buyerId))
            throw ApiException.badRequest("You cannot make an offer on your own listing");
        if (v.getStatus() != ListingStatus.ACTIVE)
            throw ApiException.badRequest("This listing is no longer accepting offers");
        if (offerRepository.existsByVehicleIdAndBuyerIdAndStatus(vehicleId, buyerId, OfferStatus.PENDING))
            throw ApiException.conflict("You already have a pending offer on this vehicle");

        User buyer = userRepository.findById(buyerId).orElseThrow(() -> ApiException.notFound("Buyer not found"));
        Offer offer = offerRepository.save(Offer.builder()
                .vehicle(v).buyer(buyer).seller(v.getSeller())
                .amount(req.amount()).message(req.message()).status(OfferStatus.PENDING).build());
        return toDto(offer);
    }

    @Transactional(readOnly = true)
    public Page<OfferDto> asBuyer(Long buyerId, int page, int size) {
        return offerRepository.findByBuyerIdOrderByCreatedAtDesc(buyerId, pageable(page, size)).map(this::toDto);
    }

    @Transactional(readOnly = true)
    public Page<OfferDto> asSeller(Long sellerId, int page, int size) {
        return offerRepository.findBySellerIdOrderByCreatedAtDesc(sellerId, pageable(page, size)).map(this::toDto);
    }

    @Transactional
    public OfferDto accept(Long offerId, Long sellerId) {
        Offer o = mustOwnAsSeller(offerId, sellerId);
        if (o.getStatus() != OfferStatus.PENDING) throw ApiException.badRequest("This offer is no longer pending");
        o.setStatus(OfferStatus.ACCEPTED);
        Vehicle v = o.getVehicle();
        if (v.getStatus() == ListingStatus.ACTIVE) v.setStatus(ListingStatus.SOLD);
        offerRepository.findByVehicleIdOrderByCreatedAtDesc(v.getId(), PageRequest.of(0, 500)).stream()
                .filter(other -> !other.getId().equals(offerId) && other.getStatus() == OfferStatus.PENDING)
                .forEach(other -> other.setStatus(OfferStatus.REJECTED));
        return toDto(o);
    }

    @Transactional
    public OfferDto reject(Long offerId, Long sellerId) {
        Offer o = mustOwnAsSeller(offerId, sellerId);
        if (o.getStatus() != OfferStatus.PENDING) throw ApiException.badRequest("This offer is no longer pending");
        o.setStatus(OfferStatus.REJECTED);
        return toDto(o);
    }

    @Transactional
    public OfferDto withdraw(Long offerId, Long buyerId) {
        Offer o = offerRepository.findById(offerId).orElseThrow(() -> ApiException.notFound("Offer not found"));
        if (!o.getBuyer().getId().equals(buyerId)) throw ApiException.forbidden("You did not make this offer");
        if (o.getStatus() != OfferStatus.PENDING) throw ApiException.badRequest("Only pending offers can be withdrawn");
        o.setStatus(OfferStatus.WITHDRAWN);
        return toDto(o);
    }

    private Offer mustOwnAsSeller(Long offerId, Long sellerId) {
        Offer o = offerRepository.findById(offerId).orElseThrow(() -> ApiException.notFound("Offer not found"));
        if (!o.getSeller().getId().equals(sellerId)) throw ApiException.forbidden("You are not the seller for this offer");
        return o;
    }

    private static Pageable pageable(int page, int size) {
        return PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50), Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    private OfferDto toDto(Offer o) {
        Vehicle v = o.getVehicle();
        return new OfferDto(o.getId(), v.getId(),
                v.getCompany() + " " + v.getModel() + " (" + v.getYear() + ")", v.getPrice(),
                o.getBuyer().getId(), o.getBuyer().getName(),
                o.getSeller().getId(), o.getSeller().getName(),
                o.getAmount(), o.getMessage(), o.getStatus(), o.getCreatedAt(), o.getUpdatedAt());
    }
}
