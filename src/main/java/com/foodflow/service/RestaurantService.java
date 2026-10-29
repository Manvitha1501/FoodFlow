package com.foodflow.service;

import com.foodflow.dto.RestaurantResponse;
import com.foodflow.dto.RestaurantUpdateRequest;
import org.springframework.data.domain.Page;

public interface RestaurantService {

    RestaurantResponse getProfile(String email);

    RestaurantResponse updateProfile(String email, RestaurantUpdateRequest request);

    void deleteProfile(String email);

    Page<RestaurantResponse> search(String name, String city, int page, int size, String sortBy, String direction);

    Page<RestaurantResponse> getAllRestaurants(int page, int size, String sortBy, String direction);

    RestaurantResponse verifyRestaurant(String id);

    RestaurantResponse deactivateRestaurant(String id);
}
