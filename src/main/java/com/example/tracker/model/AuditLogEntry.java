package com.example.tracker.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
public class AuditLogEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Instant timestamp;

    private Long patientId;

    private Long observationId;

    private String event;

    @Lob
    private String details;

    public AuditLogEntry() {}

    public AuditLogEntry(Instant timestamp, Long patientId, Long observationId, String event, String details) {
        this.timestamp = timestamp;
        this.patientId = patientId;
        this.observationId = observationId;
        this.event = event;
        this.details = details;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }
    public Long getObservationId() { return observationId; }
    public void setObservationId(Long observationId) { this.observationId = observationId; }
    public String getEvent() { return event; }
    public void setEvent(String event) { this.event = event; }
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
}
