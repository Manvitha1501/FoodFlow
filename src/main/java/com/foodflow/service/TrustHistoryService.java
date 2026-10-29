package com.foodflow.service;

import com.foodflow.document.TrustDocument;
import com.foodflow.document.TrustHistoryDocument;
import com.foodflow.dto.TrustHistoryResponse;
import com.foodflow.enums.EntityType;
import com.foodflow.enums.TriggerSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TrustHistoryService {

    TrustHistoryDocument logHistory(
            TrustDocument trustDoc,
            Double previousWeighted,
            Double previousBayesian,
            Double previousFinal,
            String calculationReason,
            TriggerSource triggerSource
    );

    List<TrustHistoryResponse> getHistory(String entityId, EntityType entityType);

    Page<TrustHistoryResponse> getHistoryPaginated(String entityId, EntityType entityType, Pageable pageable);
}
