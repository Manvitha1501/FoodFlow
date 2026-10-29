package com.foodflow.repository;

import com.foodflow.document.DonationDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DonationRepository extends MongoRepository<DonationDocument, String> {

    Optional<DonationDocument> findByIdAndIsActiveTrue(String id);

    Page<DonationDocument> findByRestaurantIdAndIsActiveTrue(String restaurantId, Pageable pageable);

    long countByNgoIdAndStatusInAndIsActiveTrue(String ngoId, java.util.List<com.foodflow.enums.DonationStatus> statuses);
}
