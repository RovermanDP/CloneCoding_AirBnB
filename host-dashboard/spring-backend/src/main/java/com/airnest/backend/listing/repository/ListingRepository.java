package com.airnest.backend.listing.repository;

import com.airnest.backend.listing.entity.Listing;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ListingRepository extends JpaRepository<Listing, Long> {

    List<Listing> findAllByOrderByUpdatedAtDescIdDesc();
}
