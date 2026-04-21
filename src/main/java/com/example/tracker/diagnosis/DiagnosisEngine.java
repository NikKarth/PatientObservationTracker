package com.example.tracker.diagnosis;

import com.example.tracker.model.*;
import com.example.tracker.model.enums.Source;
import com.example.tracker.model.enums.StrategyType;
import com.example.tracker.repository.AssociativeFunctionRepository;
import com.example.tracker.repository.ObservationRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class DiagnosisEngine {

    private final ObservationRepository observationRepository;
    private final AssociativeFunctionRepository associativeFunctionRepository;
    private final Map<StrategyType, DiagnosisStrategy> strategies;

    public DiagnosisEngine(ObservationRepository observationRepository,
                           AssociativeFunctionRepository associativeFunctionRepository,
                           Map<StrategyType, DiagnosisStrategy> strategies) {
        this.observationRepository = observationRepository;
        this.associativeFunctionRepository = associativeFunctionRepository;
        this.strategies = strategies;
    }

    public List<EvaluationResult> evaluateRulesForPatient(Patient patient) {
        List<Observation> observations = observationRepository.findByPatientOrderByRecordingTimeDesc(patient);
        List<EvaluationResult> results = new ArrayList<>();
        for (AssociativeFunction rule : associativeFunctionRepository.findAll()) {
            DiagnosisStrategy strategy = strategies.get(rule.getStrategyType());
            if (strategy != null) {
                EvaluationResult result = strategy.evaluate(rule, observations);
                if (result.isFired()) {
                    results.add(result);
                }
            }
        }
        return results;
    }
}
