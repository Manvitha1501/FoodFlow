package com.foodflow.controller;

import com.foodflow.dto.CreateDonationRequest;
import com.foodflow.dto.DonationResponse;
import com.foodflow.dto.DonationSummaryResponse;
import com.foodflow.dto.MessageResponse;
import com.foodflow.dto.UpdateDonationRequest;
import com.foodflow.enums.DonationStatus;
import com.foodflow.exception.UnauthorizedException;
import com.foodflow.service.DonationService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.time.Instant;

@RestController
@RequestMapping("/api/donations")
public class DonationController {

    private final DonationService donationService;

    public DonationController(DonationService donationService) {
        this.donationService = donationService;
    }

    @PostMapping
    public ResponseEntity<DonationResponse> createDonation(
            Principal principal,
            @Valid @RequestBody CreateDonationRequest request) {
        return ResponseEntity.ok(donationService.createDonation(principal.getName(), request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DonationResponse> updateDonation(
            Principal principal,
            @PathVariable String id,
            @Valid @RequestBody UpdateDonationRequest request) {
        return ResponseEntity.ok(donationService.updateDonation(principal.getName(), id, request));
    }

    @PutMapping("/{id}/publish")
    public ResponseEntity<DonationResponse> publishDonation(
            Principal principal,
            @PathVariable String id) {
        return ResponseEntity.ok(donationService.publishDonation(principal.getName(), id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deleteDonation(
            Principal principal,
            @PathVariable String id) {
        donationService.deleteDonation(principal.getName(), id);
        return ResponseEntity.ok(new MessageResponse("Donation deleted successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DonationResponse> getDonationById(
            Principal principal,
            @PathVariable String id) {
        return ResponseEntity.ok(donationService.getDonationById(principal.getName(), id));
    }

    @GetMapping
    public ResponseEntity<Page<DonationResponse>> getDonations(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction) {
        
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isRestaurant = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_RESTAURANT"));
        boolean isNgo = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_NGO"));

        if (isAdmin || isNgo) {
            return ResponseEntity.ok(donationService.getAllDonationsForAdmin(page, size, sortBy, direction));
        } else if (isRestaurant) {
            return ResponseEntity.ok(donationService.getMyDonations(authentication.getName(), page, size, sortBy, direction));
        }

        throw new UnauthorizedException("Access denied");
    }

    @GetMapping("/search")
    public ResponseEntity<Page<DonationSummaryResponse>> search(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) DonationStatus status,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) Instant startDate,
            @RequestParam(required = false) Instant endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction) {
        return ResponseEntity.ok(donationService.search(name, category, status, city, startDate, endDate, page, size, sortBy, direction));
    }

    @GetMapping("/filter")
    public ResponseEntity<Page<DonationSummaryResponse>> filter(
            @RequestParam String filterType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction) {
        return ResponseEntity.ok(donationService.filter(filterType, page, size, sortBy, direction));
    }

    @PutMapping("/{id}/assign/{ngoId}")
    public ResponseEntity<DonationResponse> assignNgo(
            @PathVariable String id,
            @PathVariable String ngoId) {
        return ResponseEntity.ok(donationService.assignNgo(id, ngoId));
    }
}
