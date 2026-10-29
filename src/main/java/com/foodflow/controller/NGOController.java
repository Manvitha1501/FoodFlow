package com.foodflow.controller;

import com.foodflow.dto.MessageResponse;
import com.foodflow.dto.NGOResponse;
import com.foodflow.dto.NGOUpdateRequest;
import com.foodflow.service.NGOService;
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
@RequestMapping("/api/ngo")
public class NGOController {

    private final NGOService ngoService;

    public NGOController(NGOService ngoService) {
        this.ngoService = ngoService;
    }

    @GetMapping("/profile")
    public ResponseEntity<NGOResponse> getProfile(Principal principal) {
        return ResponseEntity.ok(ngoService.getProfile(principal.getName()));
    }

    @PutMapping("/profile")
    public ResponseEntity<NGOResponse> updateProfile(
            Principal principal,
            @Valid @RequestBody NGOUpdateRequest request) {
        return ResponseEntity.ok(ngoService.updateProfile(principal.getName(), request));
    }

    @DeleteMapping("/profile")
    public ResponseEntity<MessageResponse> deleteProfile(Principal principal) {
        ngoService.deleteProfile(principal.getName());
        return ResponseEntity.ok(new MessageResponse("Profile soft-deleted successfully"));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<NGOResponse>> searchNgos(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String city,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "ngoName") String sortBy,
            @RequestParam(defaultValue = "ASC") String direction) {
        return ResponseEntity.ok(ngoService.search(name, city, page, size, sortBy, direction));
    }
}
