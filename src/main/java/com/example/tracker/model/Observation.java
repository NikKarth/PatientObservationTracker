package com.example.tracker.model;

import com.example.tracker.model.enums.Source;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Observation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Patient patient;

    private Instant recordingTime;
    private Instant applicabilityTime;

    @ManyToOne
    private Protocol protocol;

    @Enumerated(EnumType.STRING)
    private ObservationStatus status = ObservationStatus.ACTIVE;

    private String rejectionReason;

    private boolean anomaly = false;

    @Enumerated(EnumType.STRING)
    private Source source = Source.MANUAL;

    public Observation() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Patient getPatient() { return patient; }
    public void setPatient(Patient patient) { this.patient = patient; }
    public Instant getRecordingTime() { return recordingTime; }
    public void setRecordingTime(Instant recordingTime) { this.recordingTime = recordingTime; }
    public Instant getApplicabilityTime() { return applicabilityTime; }
    public void setApplicabilityTime(Instant applicabilityTime) { this.applicabilityTime = applicabilityTime; }
    public Protocol getProtocol() { return protocol; }
    public void setProtocol(Protocol protocol) { this.protocol = protocol; }
    public ObservationStatus getStatus() { return status; }
    public void setStatus(ObservationStatus status) { this.status = status; }
    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
    public boolean isAnomaly() { return anomaly; }
    public void setAnomaly(boolean anomaly) { this.anomaly = anomaly; }
    public Source getSource() { return source; }
    public void setSource(Source source) { this.source = source; }
}
