package com.foodflow.service;

import org.springframework.security.core.userdetails.UserDetails;

import java.util.Map;

public interface JwtService {

    String generateToken(UserDetails userDetails);

    String generateToken(Map<String, Object> extraClaims, UserDetails userDetails);

    String extractEmail(String token);

    boolean isTokenValid(String token, UserDetails userDetails);

    Long getExpirationTime();
}
