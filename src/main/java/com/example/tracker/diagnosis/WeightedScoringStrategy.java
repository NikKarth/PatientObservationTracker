package com.example.tracker.diagnosis;

import com.example.tracker.model.*;
import com.example.tracker.model.enums.StrategyType;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class WeightedScoringStrategy implements DiagnosisStrategy {

    @Override
    public EvaluationResult evaluate(AssociativeFunction rule, List<Observation> patientObservations) {
        double sum = 0;
        List<Observation> evidence = new ArrayList<>();
        for (ArgumentWeight aw : rule.getArguments()) {
            Optional<Observation> obs = patientObservations.stream()
                .filter(o -> o.getStatus() == ObservationStatus.ACTIVE && o.getSource() == com.example.tracker.model.enums.Source.MANUAL)
                .filter(o -> matches(o, aw.getConcept()))
                .findFirst();
            if (obs.isPresent()) {
                sum += aw.getWeight();
                evidence.add(obs.get());
            }
        }
        boolean fired = sum > rule.getThreshold();
        return new EvaluationResult(rule.getProductConcept(), fired, StrategyType.WEIGHTED, evidence);
    }

    private boolean matches(Observation o, String concept) {
        if (o instanceof CategoryObservation co && co.getPresence() == Presence.PRESENT) {
            return co.getPhenomenon().getName().equals(concept);
        } else if (o instanceof Measurement m) {
            return m.getPhenomenonType().getName().equals(concept);
        }
        return false;
    }
}