package com.example.tracker.model;

import jakarta.persistence.*;

@Entity
public class Protocol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Lob
    private String description;

    @Enumerated(EnumType.STRING)
    private AccuracyRating accuracyRating;

    public Protocol() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public AccuracyRating getAccuracyRating() { return accuracyRating; }
    public void setAccuracyRating(AccuracyRating accuracyRating) { this.accuracyRating = accuracyRating; }
}
