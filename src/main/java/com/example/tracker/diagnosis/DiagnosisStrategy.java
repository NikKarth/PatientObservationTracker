package com.example.tracker.diagnosis;

import com.example.tracker.model.AssociativeFunction;
import com.example.tracker.model.Observation;

import java.util.List;

public interface DiagnosisStrategy {
    boolean evaluate(AssociativeFunction rule, List<Observation> patientObservations);
}
