package com.foodflow.repository;

import com.foodflow.document.TrustHistoryDocument;
import com.foodflow.enums.EntityType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrustHistoryRepository extends MongoRepository<TrustHistoryDocument, String> {

    List<TrustHistoryDocument> findByEntityIdAndEntityTypeOrderByCalculatedAtDesc(String entityId, EntityType entityType);

    Page<TrustHistoryDocument> findByEntityIdAndEntityTypeOrderByCalculatedAtDesc(String entityId, EntityType entityType, Pageable pageable);

    Page<TrustHistoryDocument> findAllByOrderByCalculatedAtDesc(Pageable pageable);
}
