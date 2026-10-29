package com.foodflow.controller;

import com.foodflow.dto.NGOResponse;
import com.foodflow.dto.RestaurantResponse;
import com.foodflow.service.NGOService;
import com.foodflow.service.RestaurantService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminProfileController {

    private final RestaurantService restaurantService;
    private final NGOService ngoService;

    public AdminProfileController(RestaurantService restaurantService, NGOService ngoService) {
        this.restaurantService = restaurantService;
        this.ngoService = ngoService;
    }

    @GetMapping("/restaurants")
    public ResponseEntity<Page<RestaurantResponse>> getAllRestaurants(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "restaurantName") String sortBy,
            @RequestParam(defaultValue = "ASC") String direction) {
        return ResponseEntity.ok(restaurantService.getAllRestaurants(page, size, sortBy, direction));
    }

    @GetMapping("/ngos")
    public ResponseEntity<Page<NGOResponse>> getAllNgos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "ngoName") String sortBy,
            @RequestParam(defaultValue = "ASC") String direction) {
        return ResponseEntity.ok(ngoService.getAllNgos(page, size, sortBy, direction));
    }

    @PutMapping("/restaurant/verify/{id}")
    public ResponseEntity<RestaurantResponse> verifyRestaurant(@PathVariable String id) {
        return ResponseEntity.ok(restaurantService.verifyRestaurant(id));
    }

    @PutMapping("/ngo/verify/{id}")
    public ResponseEntity<NGOResponse> verifyNgo(@PathVariable String id) {
        return ResponseEntity.ok(ngoService.verifyNgo(id));
    }

    @PutMapping("/restaurant/deactivate/{id}")
    public ResponseEntity<RestaurantResponse> deactivateRestaurant(@PathVariable String id) {
        return ResponseEntity.ok(restaurantService.deactivateRestaurant(id));
    }

    @PutMapping("/ngo/deactivate/{id}")
    public ResponseEntity<NGOResponse> deactivateNgo(@PathVariable String id) {
        return ResponseEntity.ok(ngoService.deactivateNgo(id));
    }
}
