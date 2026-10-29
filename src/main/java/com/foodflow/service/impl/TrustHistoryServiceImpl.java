package com.foodflow.service.impl;

import com.foodflow.document.TrustDocument;
import com.foodflow.document.TrustHistoryDocument;
import com.foodflow.dto.TrustHistoryResponse;
import com.foodflow.enums.EntityType;
import com.foodflow.enums.TriggerSource;
import com.foodflow.repository.TrustHistoryRepository;
import com.foodflow.service.TrustHistoryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TrustHistoryServiceImpl implements TrustHistoryService {

    private final TrustHistoryRepository historyRepository;

    public TrustHistoryServiceImpl(TrustHistoryRepository historyRepository) {
        this.historyRepository = historyRepository;
    }

    @Override
    public TrustHistoryDocument logHistory(
            TrustDocument trustDoc,
            Double previousWeighted,
            Double previousBayesian,
            Double previousFinal,
            String calculationReason,
            TriggerSource triggerSource
    ) {
        TrustHistoryDocument history = TrustHistoryDocument.builder()
                .trustId(trustDoc.getId())
                .entityId(trustDoc.getEntityId())
                .entityType(trustDoc.getEntityType())
                .previousWeightedScore(previousWeighted != null ? previousWeighted : 0.0)
                .newWeightedScore(trustDoc.getWeightedScore())
                .previousBayesianScore(previousBayesian != null ? previousBayesian : 0.0)
                .newBayesianScore(trustDoc.getBayesianScore())
                .previousFinalScore(previousFinal != null ? previousFinal : 0.0)
                .newFinalScore(trustDoc.getFinalTrustScore())
                .calculationReason(calculationReason)
                .triggerSource(triggerSource)
                .calculatedAt(Instant.now())
                .build();

        return historyRepository.save(history);
    }

    @Override
    public List<TrustHistoryResponse> getHistory(String entityId, EntityType entityType) {
        return historyRepository
                .findByEntityIdAndEntityTypeOrderByCalculatedAtDesc(entityId, entityType)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Page<TrustHistoryResponse> getHistoryPaginated(String entityId, EntityType entityType, Pageable pageable) {
        return historyRepository
                .findByEntityIdAndEntityTypeOrderByCalculatedAtDesc(entityId, entityType, pageable)
                .map(this::mapToResponse);
    }

    private TrustHistoryResponse mapToResponse(TrustHistoryDocument doc) {
        return TrustHistoryResponse.builder()
                .id(doc.getId())
                .trustId(doc.getTrustId())
                .entityId(doc.getEntityId())
                .entityType(doc.getEntityType())
                .previousWeightedScore(doc.getPreviousWeightedScore())
                .newWeightedScore(doc.getNewWeightedScore())
                .previousBayesianScore(doc.getPreviousBayesianScore())
                .newBayesianScore(doc.getNewBayesianScore())
                .previousFinalScore(doc.getPreviousFinalScore())
                .newFinalScore(doc.getNewFinalScore())
                .calculationReason(doc.getCalculationReason())
                .triggerSource(doc.getTriggerSource())
                .calculatedAt(doc.getCalculatedAt())
                .build();
    }
}
