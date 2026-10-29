package com.foodflow.repository;

import com.foodflow.document.RestaurantProfileDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RestaurantProfileRepository extends MongoRepository<RestaurantProfileDocument, String> {

    Optional<RestaurantProfileDocument> findByUserId(String userId);

    Optional<RestaurantProfileDocument> findByUserIdAndIsDeletedFalse(String userId);

    boolean existsByLicenseNumber(String licenseNumber);

    boolean existsByLicenseNumberAndUserIdNot(String licenseNumber, String userId);

    Page<RestaurantProfileDocument> findByRestaurantNameContainingIgnoreCaseAndIsDeletedFalse(String name, Pageable pageable);

    Page<RestaurantProfileDocument> findByRestaurantNameContainingIgnoreCaseAndCityIgnoreCaseAndIsDeletedFalse(String name, String city, Pageable pageable);

    Page<RestaurantProfileDocument> findByCityIgnoreCaseAndIsDeletedFalse(String city, Pageable pageable);

    Page<RestaurantProfileDocument> findByIsDeletedFalse(Pageable pageable);
}
