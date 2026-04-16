package com.example.tracker.diagnosis;

import com.example.tracker.model.*;
import com.example.tracker.repository.AssociativeFunctionRepository;
import com.example.tracker.repository.ObservationRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class DiagnosisEngine {

    private final ObservationRepository observationRepository;
    private final AssociativeFunctionRepository associativeFunctionRepository;
    private final DiagnosisStrategy strategy;

    public DiagnosisEngine(ObservationRepository observationRepository,
                           AssociativeFunctionRepository associativeFunctionRepository,
                           DiagnosisStrategy strategy) {
        this.observationRepository = observationRepository;
        this.associativeFunctionRepository = associativeFunctionRepository;
        this.strategy = strategy;
    }

    public List<String> evaluateRulesForPatient(Patient patient) {
        List<Observation> observations = observationRepository.findByPatientOrderByRecordingTimeDesc(patient);
        List<String> inferences = new ArrayList<>();
        for (AssociativeFunction rule : associativeFunctionRepository.findAll()) {
            if (strategy.evaluate(rule, observations)) {
                String product = rule.getProductConcept();
                if (product != null && !inferences.contains(product)) {
                    inferences.add(product);
                }
            }
        }
        return inferences;
    }
}

interface DiagnosisStrategy {
    boolean evaluate(AssociativeFunction rule, List<Observation> patientObservations);
}

@Service
class SimpleConjunctiveStrategy implements DiagnosisStrategy {

    @Override
    public boolean evaluate(AssociativeFunction rule, List<Observation> patientObservations) {
        Set<String> observed = new HashSet<>();
        for (Observation o : patientObservations) {
            if (o.getStatus() == ObservationStatus.REJECTED) continue;
            if (o instanceof Measurement) {
                Measurement m = (Measurement) o;
                if (m.getPhenomenonType() != null && m.getPhenomenonType().getName() != null) {
                    observed.add(m.getPhenomenonType().getName());
                }
            } else if (o instanceof CategoryObservation) {
                CategoryObservation c = (CategoryObservation) o;
                if (c.getPresence() == Presence.PRESENT && c.getPhenomenon() != null && c.getPhenomenon().getName() != null) {
                    observed.add(c.getPhenomenon().getName());
                }
            }
        }
        for (String arg : rule.getArgumentConcepts()) {
            if (!observed.contains(arg)) return false;
        }
        return true;
    }
}
