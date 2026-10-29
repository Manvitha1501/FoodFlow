package com.foodflow.service;

import com.foodflow.dto.JwtResponse;
import com.foodflow.dto.LoginRequest;
import com.foodflow.dto.RegisterRequest;

public interface AuthService {

    JwtResponse register(RegisterRequest request);

    JwtResponse login(LoginRequest request);

    JwtResponse adminLogin(LoginRequest request);
}
