package com.foodflow.factory;

import com.foodflow.algorithm.AllocationStrategy;
import com.foodflow.algorithm.impl.GreedyAllocationStrategy;
import com.foodflow.algorithm.impl.LinearProgrammingAllocationStrategy;
import com.foodflow.enums.AllocationAlgorithm;
import org.springframework.stereotype.Component;

@Component
public class AllocationStrategyFactory {

    private final GreedyAllocationStrategy greedyAllocationStrategy;
    private final LinearProgrammingAllocationStrategy linearProgrammingAllocationStrategy;

    public AllocationStrategyFactory(
            GreedyAllocationStrategy greedyAllocationStrategy,
            LinearProgrammingAllocationStrategy linearProgrammingAllocationStrategy) {
        this.greedyAllocationStrategy = greedyAllocationStrategy;
        this.linearProgrammingAllocationStrategy = linearProgrammingAllocationStrategy;
    }

    public AllocationStrategy getStrategy(AllocationAlgorithm algorithm) {
        if (algorithm == null) {
            return greedyAllocationStrategy;
        }
        return switch (algorithm) {
            case GREEDY -> greedyAllocationStrategy;
            case LINEAR_PROGRAMMING -> linearProgrammingAllocationStrategy;
        };
    }
}
