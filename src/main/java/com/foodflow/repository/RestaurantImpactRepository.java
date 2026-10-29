package com.foodflow.repository;

import com.foodflow.document.RestaurantImpactDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RestaurantImpactRepository extends MongoRepository<RestaurantImpactDocument, String> {

    Optional<RestaurantImpactDocument> findByRestaurantId(String restaurantId);

    List<RestaurantImpactDocument> findAllByOrderByTotalFoodSavedKgDesc();
}
