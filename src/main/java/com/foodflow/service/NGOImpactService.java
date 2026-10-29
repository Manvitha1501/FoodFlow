package com.foodflow.service;

import com.foodflow.dto.NGOImpactResponse;

import java.util.List;

public interface NGOImpactService {

    NGOImpactResponse updateNgoImpactOnWorkflowCompletion(String ngoId, double foodSavedKg);

    NGOImpactResponse getNgoImpact(String ngoId);

    List<NGOImpactResponse> getTopNgoContributors();
}
