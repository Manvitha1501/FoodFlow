package com.foodflow.controller;

import com.foodflow.dto.MessageResponse;
import com.foodflow.dto.RestaurantResponse;
import com.foodflow.dto.RestaurantUpdateRequest;
import com.foodflow.service.RestaurantService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/restaurant")
public class RestaurantController {

    private final RestaurantService restaurantService;

    public RestaurantController(RestaurantService restaurantService) {
        this.restaurantService = restaurantService;
    }

    @GetMapping("/profile")
    public ResponseEntity<RestaurantResponse> getProfile(Principal principal) {
        return ResponseEntity.ok(restaurantService.getProfile(principal.getName()));
    }

    @PutMapping("/profile")
    public ResponseEntity<RestaurantResponse> updateProfile(
            Principal principal,
            @Valid @RequestBody RestaurantUpdateRequest request) {
        return ResponseEntity.ok(restaurantService.updateProfile(principal.getName(), request));
    }

    @DeleteMapping("/profile")
    public ResponseEntity<MessageResponse> deleteProfile(Principal principal) {
        restaurantService.deleteProfile(principal.getName());
        return ResponseEntity.ok(new MessageResponse("Profile soft-deleted successfully"));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<RestaurantResponse>> searchRestaurants(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String city,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "restaurantName") String sortBy,
            @RequestParam(defaultValue = "ASC") String direction) {
        return ResponseEntity.ok(restaurantService.search(name, city, page, size, sortBy, direction));
    }
}
