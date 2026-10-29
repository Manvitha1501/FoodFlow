package com.foodflow.algorithm.impl;

import com.foodflow.algorithm.AllocationStrategy;
import com.foodflow.document.DonationDocument;
import com.foodflow.document.EmbeddedAllocation;
import com.foodflow.document.NgoProfileDocument;
import com.foodflow.document.RankedNGO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class LinearProgrammingAllocationStrategy implements AllocationStrategy {

    @Override
    public List<EmbeddedAllocation> allocate(
            DonationDocument donation,
            Map<String, NgoProfileDocument> activeNgosMap,
            List<RankedNGO> matchingRanks
    ) {
        throw new UnsupportedOperationException("Linear Programming allocation strategy is not implemented yet");
    }
}
