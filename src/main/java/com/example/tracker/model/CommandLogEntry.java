package com.example.tracker.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
public class CommandLogEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String commandType;

    @Lob
    private String payload;

    private Instant executedAt;

    private String user;

    public CommandLogEntry() {}

    public CommandLogEntry(String commandType, String payload, Instant executedAt, String user) {
        this.commandType = commandType;
        this.payload = payload;
        this.executedAt = executedAt;
        this.user = user;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCommandType() { return commandType; }
    public void setCommandType(String commandType) { this.commandType = commandType; }
    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }
    public Instant getExecutedAt() { return executedAt; }
    public void setExecutedAt(Instant executedAt) { this.executedAt = executedAt; }
    public String getUser() { return user; }
    public void setUser(String user) { this.user = user; }
}
