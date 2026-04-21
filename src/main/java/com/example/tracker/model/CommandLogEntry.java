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

    @ManyToOne
    private User user;

    private boolean undone = false;

    public CommandLogEntry() {}

    public CommandLogEntry(String commandType, String payload, Instant executedAt, User user) {
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
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public boolean isUndone() { return undone; }
    public void setUndone(boolean undone) { this.undone = undone; }
}
