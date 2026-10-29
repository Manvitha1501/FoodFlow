package com.foodflow.repository;

import com.foodflow.document.DonationAllocationDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DonationAllocationRepository extends MongoRepository<DonationAllocationDocument, String> {

    Optional<DonationAllocationDocument> findTopByDonationIdOrderByCreatedAtDesc(String donationId);

    Page<DonationAllocationDocument> findByRestaurantIdOrderByCreatedAtDesc(String restaurantId, Pageable pageable);

    Page<DonationAllocationDocument> findByAllocationsNgoIdOrderByCreatedAtDesc(String ngoId, Pageable pageable);

    Page<DonationAllocationDocument> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
