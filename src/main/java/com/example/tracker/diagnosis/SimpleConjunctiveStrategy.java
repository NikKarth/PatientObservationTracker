package com.example.tracker.diagnosis;

import com.example.tracker.model.*;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class SimpleConjunctiveStrategy implements DiagnosisStrategy {

    @Override
    public boolean evaluate(AssociativeFunction rule, List<Observation> patientObservations) {
        // Build set of observed concept names from active observations
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

        // Conjunctive: all argument concepts must be present
        for (String arg : rule.getArgumentConcepts()) {
            if (!observed.contains(arg)) return false;
        }
        return true;
    }
}
