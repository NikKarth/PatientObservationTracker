package com.example.tracker.diagnosis;

import com.example.tracker.model.*;
import com.example.tracker.model.enums.StrategyType;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class SimpleConjunctiveStrategy implements DiagnosisStrategy {

    @Override
    public EvaluationResult evaluate(AssociativeFunction rule, List<Observation> patientObservations) {
        List<String> args = rule.getArguments().stream().map(ArgumentWeight::getConcept).toList();
        List<Observation> evidence = patientObservations.stream()
            .filter(o -> o.getStatus() == ObservationStatus.ACTIVE && o.getSource() == com.example.tracker.model.enums.Source.MANUAL)
            .filter(o -> {
                if (o instanceof CategoryObservation co && co.getPresence() == Presence.PRESENT) {
                    return args.contains(co.getPhenomenon().getName());
                } else if (o instanceof Measurement m) {
                    return args.contains(m.getPhenomenonType().getName());
                }
                return false;
            })
            .toList();
        boolean fired = args.stream().allMatch(arg -> evidence.stream().anyMatch(e -> matches(e, arg)));
        return new EvaluationResult(rule.getProductConcept(), fired, StrategyType.CONJUNCTIVE, evidence);
    }

    private boolean matches(Observation o, String concept) {
        if (o instanceof CategoryObservation co) {
            return co.getPhenomenon().getName().equals(concept);
        } else if (o instanceof Measurement m) {
            return m.getPhenomenonType().getName().equals(concept);
        }
        return false;
    }
}