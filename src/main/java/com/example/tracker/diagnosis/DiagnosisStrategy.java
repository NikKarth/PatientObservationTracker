package com.example.tracker.diagnosis;

import com.example.tracker.model.AssociativeFunction;
import com.example.tracker.model.Observation;
import java.util.List;

public interface DiagnosisStrategy {

    EvaluationResult evaluate(AssociativeFunction rule, List<Observation> patientObservations);

}