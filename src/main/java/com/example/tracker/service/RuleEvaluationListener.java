package com.example.tracker.service;

import com.example.tracker.diagnosis.DiagnosisEngine;
import com.example.tracker.event.ObservationSavedEvent;
import com.example.tracker.model.AuditLogEntry;
import com.example.tracker.model.Observation;
import com.example.tracker.repository.AuditLogRepository;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

@Service
public class RuleEvaluationListener {

    private final DiagnosisEngine diagnosisEngine;
    private final AuditLogRepository auditLogRepository;
    private final Clock clock;

    public RuleEvaluationListener(DiagnosisEngine diagnosisEngine,
                                  AuditLogRepository auditLogRepository,
                                  Clock clock) {
        this.diagnosisEngine = diagnosisEngine;
        this.auditLogRepository = auditLogRepository;
        this.clock = clock;
    }

    @EventListener
    public void onObservationSaved(ObservationSavedEvent event) {
        Observation observation = event.getObservation();
        List<String> inferences = diagnosisEngine.evaluateRulesForPatient(observation.getPatient());
        for (String inference : inferences) {
            AuditLogEntry entry = new AuditLogEntry(
                    Instant.now(clock),
                    observation.getPatient() != null ? observation.getPatient().getId() : null,
                    observation.getId(),
                    "RuleEvaluation",
                    inference
            );
            auditLogRepository.save(entry);
        }
    }
}
