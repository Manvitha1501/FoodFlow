package com.foodflow.controller;

import com.foodflow.dto.JwtResponse;
import com.foodflow.dto.LoginRequest;
import com.foodflow.dto.MessageResponse;
import com.foodflow.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AuthService authService;

    public AdminController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@Valid @RequestBody LoginRequest request) {
        JwtResponse response = authService.adminLogin(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/dashboard")
    public ResponseEntity<MessageResponse> getDashboard() {
        return ResponseEntity.ok(new MessageResponse("Welcome to the Admin Command Center. System status: NORMAL. Matching Accuracy: 98.4%"));
    }
}
