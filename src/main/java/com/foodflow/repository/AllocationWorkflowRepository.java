package com.foodflow.repository;

import com.foodflow.document.AllocationWorkflowDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AllocationWorkflowRepository extends MongoRepository<AllocationWorkflowDocument, String> {

    Optional<AllocationWorkflowDocument> findByAllocationIdAndNgoId(String allocationId, String ngoId);

    List<AllocationWorkflowDocument> findByDonationId(String donationId);

    Page<AllocationWorkflowDocument> findByRestaurantIdOrderByCreatedAtDesc(String restaurantId, Pageable pageable);

    Page<AllocationWorkflowDocument> findByNgoIdOrderByCreatedAtDesc(String ngoId, Pageable pageable);

    Page<AllocationWorkflowDocument> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
