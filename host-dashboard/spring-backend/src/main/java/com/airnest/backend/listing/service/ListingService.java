package com.airnest.backend.listing.service;

import com.airnest.backend.common.exception.InvalidRequestException;
import com.airnest.backend.common.exception.ResourceNotFoundException;
import com.airnest.backend.listing.dto.ListingListResponse;
import com.airnest.backend.listing.dto.ListingResponse;
import com.airnest.backend.listing.entity.Listing;
import com.airnest.backend.listing.entity.ListingStatus;
import com.airnest.backend.listing.repository.ListingRepository;
import java.time.Instant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListingService {

    private final ListingRepository listingRepository;

    public ListingService(ListingRepository listingRepository) {
        this.listingRepository = listingRepository;
    }

    @Transactional(readOnly = true)
    public ListingListResponse listListings(int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(
            Sort.Order.desc("updatedAt"),
            Sort.Order.desc("id")
        ));
        Page<Listing> result = listingRepository.findAll(pageable);
        return new ListingListResponse(
            result.getContent().stream().map(ListingResponse::from).toList(),
            result.getNumber(),
            result.getSize(),
            result.getTotalElements(),
            result.getTotalPages(),
            result.isLast()
        );
    }

    @Transactional
    public ListingResponse updateStatus(Long listingId, String rawStatus) {
        ListingStatus nextStatus;
        try {
            nextStatus = ListingStatus.fromValue(rawStatus.trim());
        } catch (IllegalArgumentException exception) {
            throw new InvalidRequestException("Listing status is invalid.");
        }

        Listing listing = listingRepository.findById(listingId)
            .orElseThrow(() -> new ResourceNotFoundException("Listing not found."));

        listing.updateStatus(nextStatus, Instant.now());
        return ListingResponse.from(listing);
    }
}
