package com.foodflow.repository;

import com.foodflow.document.DonationRiskPredictionDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DonationRiskPredictionRepository extends MongoRepository<DonationRiskPredictionDocument, String> {

    Optional<DonationRiskPredictionDocument> findByDonationIdAndNgoId(String donationId, String ngoId);

    List<DonationRiskPredictionDocument> findByDonationId(String donationId);

    Page<DonationRiskPredictionDocument> findByRestaurantIdOrderByCreatedAtDesc(String restaurantId, Pageable pageable);

    Page<DonationRiskPredictionDocument> findByNgoIdOrderByCreatedAtDesc(String ngoId, Pageable pageable);

    Page<DonationRiskPredictionDocument> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
