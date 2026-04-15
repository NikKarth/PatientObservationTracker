package com.example.tracker.model;

import jakarta.persistence.*;

@Entity
public class Phenomenon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToOne(optional = false)
    private PhenomenonType phenomenonType;

    public Phenomenon() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public PhenomenonType getPhenomenonType() { return phenomenonType; }
    public void setPhenomenonType(PhenomenonType phenomenonType) { this.phenomenonType = phenomenonType; }
}
