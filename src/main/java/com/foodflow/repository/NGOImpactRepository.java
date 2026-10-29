package com.foodflow.repository;

import com.foodflow.document.NGOImpactDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NGOImpactRepository extends MongoRepository<NGOImpactDocument, String> {

    Optional<NGOImpactDocument> findByNgoId(String ngoId);

    List<NGOImpactDocument> findAllByOrderByTotalMealsReceivedDesc();
}
