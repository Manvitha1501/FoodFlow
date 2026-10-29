package com.foodflow.service;

import com.foodflow.dto.CreateDonationRequest;
import com.foodflow.dto.DonationResponse;
import com.foodflow.dto.DonationSummaryResponse;
import com.foodflow.dto.UpdateDonationRequest;
import com.foodflow.enums.DonationStatus;
import org.springframework.data.domain.Page;

import java.time.Instant;

public interface DonationService {

    DonationResponse createDonation(String email, CreateDonationRequest request);

    DonationResponse updateDonation(String email, String id, UpdateDonationRequest request);

    DonationResponse publishDonation(String email, String id);

    void deleteDonation(String email, String id);

    DonationResponse getDonationById(String email, String id);

    Page<DonationResponse> getMyDonations(String email, int page, int size, String sortBy, String direction);

    Page<DonationResponse> getAllDonationsForAdmin(int page, int size, String sortBy, String direction);

    Page<DonationSummaryResponse> search(String name, String category, DonationStatus status, String city, Instant startDate, Instant endDate, int page, int size, String sortBy, String direction);

    Page<DonationSummaryResponse> filter(String filterType, int page, int size, String sortBy, String direction);

    DonationResponse assignNgo(String id, String ngoId);
}
