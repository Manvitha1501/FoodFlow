package com.foodflow.repository;

import com.foodflow.document.NgoProfileDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NgoProfileRepository extends MongoRepository<NgoProfileDocument, String> {

    Optional<NgoProfileDocument> findByUserId(String userId);

    Optional<NgoProfileDocument> findByUserIdAndIsDeletedFalse(String userId);

    boolean existsByRegistrationNumber(String registrationNumber);

    boolean existsByRegistrationNumberAndUserIdNot(String registrationNumber, String userId);

    Page<NgoProfileDocument> findByNgoNameContainingIgnoreCaseAndIsDeletedFalse(String name, Pageable pageable);

    Page<NgoProfileDocument> findByNgoNameContainingIgnoreCaseAndCityIgnoreCaseAndIsDeletedFalse(String name, String city, Pageable pageable);

    Page<NgoProfileDocument> findByCityIgnoreCaseAndIsDeletedFalse(String city, Pageable pageable);

    Page<NgoProfileDocument> findByIsDeletedFalse(Pageable pageable);
}
