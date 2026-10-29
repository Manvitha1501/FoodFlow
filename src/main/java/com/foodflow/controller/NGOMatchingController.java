package com.foodflow.controller;

import com.foodflow.dto.NGOMatchResponse;
import com.foodflow.dto.MatchingSummaryResponse;
import com.foodflow.service.NGOMatchingService;
import com.foodflow.validation.NGOMatchingValidator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/matching")
public class NGOMatchingController {

    private final NGOMatchingService ngoMatchingService;
    private final NGOMatchingValidator matchingValidator;

    public NGOMatchingController(NGOMatchingService ngoMatchingService, NGOMatchingValidator matchingValidator) {
        this.ngoMatchingService = ngoMatchingService;
        this.matchingValidator = matchingValidator;
    }

    @PostMapping("/{donationId}")
    public ResponseEntity<NGOMatchResponse> generateMatching(
            Principal principal,
            @PathVariable String donationId) {
        matchingValidator.validateObjectId(donationId, "donationId");
        return ResponseEntity.ok(ngoMatchingService.generateMatching(principal.getName(), donationId));
    }

    @GetMapping("/{donationId}")
    public ResponseEntity<NGOMatchResponse> getMatchingResult(
            Principal principal,
            @PathVariable String donationId) {
        matchingValidator.validateObjectId(donationId, "donationId");
        return ResponseEntity.ok(ngoMatchingService.getLatestMatching(principal.getName(), donationId));
    }

    @GetMapping("/history")
    public ResponseEntity<Page<MatchingSummaryResponse>> getMatchingHistory(
            Principal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        // Enforce safe pagination bounds
        int sanitizedPage = Math.max(0, page);
        int sanitizedSize = Math.min(100, Math.max(1, size));
        Pageable pageable = PageRequest.of(sanitizedPage, sanitizedSize);
        return ResponseEntity.ok(ngoMatchingService.getMatchingHistory(principal.getName(), pageable));
    }
}
