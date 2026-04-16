package com.example.tracker.service;

import com.example.tracker.event.ObservationSavedEvent;
import com.example.tracker.model.AuditLogEntry;
import com.example.tracker.model.Observation;
import com.example.tracker.repository.AuditLogRepository;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.time.Clock;
import java.time.Instant;

@Service
public class AuditLogListener {

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    public AuditLogListener(AuditLogRepository auditLogRepository, ObjectMapper objectMapper, Clock clock) {
        this.auditLogRepository = auditLogRepository;
        this.objectMapper = objectMapper;
        this.clock = clock;
    }

    @EventListener
    public void onObservationSaved(ObservationSavedEvent event) {
        Observation observation = event.getObservation();
        String details = toJson(observation);
        AuditLogEntry entry = new AuditLogEntry(
                Instant.now(clock),
                observation.getPatient() != null ? observation.getPatient().getId() : null,
                observation.getId(),
                observation.getStatus() == null ? "ObservationSaved" : observation.getStatus().name(),
                details
        );
        auditLogRepository.save(entry);
    }

    private String toJson(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JacksonException e) {
            return String.format("{\"error\": \"unable to serialize audit detail: %s\"}", e.getMessage());
        }
    }
}
