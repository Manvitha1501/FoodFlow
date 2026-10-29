package com.foodflow.repository;

import com.foodflow.document.DonationPredictionDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DonationPredictionRepository extends MongoRepository<DonationPredictionDocument, String> {

    Optional<DonationPredictionDocument> findTopByDonationIdOrderByCreatedAtDesc(String donationId);

    List<DonationPredictionDocument> findByDonationIdOrderByCreatedAtDesc(String donationId);

    Page<DonationPredictionDocument> findByRestaurantIdOrderByCreatedAtDesc(String restaurantId, Pageable pageable);

    Page<DonationPredictionDocument> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
