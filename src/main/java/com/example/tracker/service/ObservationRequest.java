package com.example.tracker.service;

import com.example.tracker.model.*;
import java.time.Instant;

public class ObservationRequest {

    private Patient patient;
    private PhenomenonType phenomenonType;
    private Quantity quantity;
    private Phenomenon phenomenon;
    private Presence presence;
    private Protocol protocol;
    private Instant applicabilityTime;
    private String user;
    private boolean anomaly = false;

    // getters and setters

    public Patient getPatient() { return patient; }
    public void setPatient(Patient patient) { this.patient = patient; }
    public PhenomenonType getPhenomenonType() { return phenomenonType; }
    public void setPhenomenonType(PhenomenonType phenomenonType) { this.phenomenonType = phenomenonType; }
    public Quantity getQuantity() { return quantity; }
    public void setQuantity(Quantity quantity) { this.quantity = quantity; }
    public Phenomenon getPhenomenon() { return phenomenon; }
    public void setPhenomenon(Phenomenon phenomenon) { this.phenomenon = phenomenon; }
    public Presence getPresence() { return presence; }
    public void setPresence(Presence presence) { this.presence = presence; }
    public Protocol getProtocol() { return protocol; }
    public void setProtocol(Protocol protocol) { this.protocol = protocol; }
    public Instant getApplicabilityTime() { return applicabilityTime; }
    public void setApplicabilityTime(Instant applicabilityTime) { this.applicabilityTime = applicabilityTime; }
    public String getUser() { return user; }
    public void setUser(String user) { this.user = user; }
    public boolean isAnomaly() { return anomaly; }
    public void setAnomaly(boolean anomaly) { this.anomaly = anomaly; }
    public Instant getRecordingTime() { return recordingTime; }
    public void setRecordingTime(Instant recordingTime) { this.recordingTime = recordingTime; }

    private Instant recordingTime;
}