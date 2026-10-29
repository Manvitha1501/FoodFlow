package com.foodflow.service;

import com.foodflow.dto.NGOResponse;
import com.foodflow.dto.NGOUpdateRequest;
import org.springframework.data.domain.Page;

public interface NGOService {

    NGOResponse getProfile(String email);

    NGOResponse updateProfile(String email, NGOUpdateRequest request);

    void deleteProfile(String email);

    Page<NGOResponse> search(String name, String city, int page, int size, String sortBy, String direction);

    Page<NGOResponse> getAllNgos(int page, int size, String sortBy, String direction);

    NGOResponse verifyNgo(String id);

    NGOResponse deactivateNgo(String id);
}
