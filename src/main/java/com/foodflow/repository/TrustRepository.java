package com.foodflow.repository;

import com.foodflow.document.TrustDocument;
import com.foodflow.enums.EntityType;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrustRepository extends MongoRepository<TrustDocument, String> {

    Optional<TrustDocument> findByEntityIdAndEntityType(String entityId, EntityType entityType);

    List<TrustDocument> findByEntityTypeOrderByFinalTrustScoreDesc(EntityType entityType);
}
