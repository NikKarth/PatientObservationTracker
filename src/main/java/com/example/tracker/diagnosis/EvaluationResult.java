package com.example.tracker.diagnosis;

import com.example.tracker.model.Observation;
import com.example.tracker.model.enums.StrategyType;
import java.util.List;

public class EvaluationResult {

    private final String productConcept;
    private final boolean fired;
    private final StrategyType strategy;
    private final List<Observation> evidence;

    public EvaluationResult(String productConcept, boolean fired, StrategyType strategy, List<Observation> evidence) {
        this.productConcept = productConcept;
        this.fired = fired;
        this.strategy = strategy;
        this.evidence = evidence;
    }

    public String getProductConcept() { return productConcept; }
    public boolean isFired() { return fired; }
    public StrategyType getStrategy() { return strategy; }
    public List<Observation> getEvidence() { return evidence; }
}