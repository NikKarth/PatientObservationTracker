package com.example.tracker.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Phenomenon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToOne(optional = false)
    private PhenomenonType phenomenonType;

    @ManyToOne
    private Phenomenon parentConcept;

    @OneToMany(mappedBy = "parentConcept", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Phenomenon> children = new ArrayList<>();

    public Phenomenon() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public PhenomenonType getPhenomenonType() { return phenomenonType; }
    public void setPhenomenonType(PhenomenonType phenomenonType) { this.phenomenonType = phenomenonType; }
    public Phenomenon getParentConcept() { return parentConcept; }
    public void setParentConcept(Phenomenon parentConcept) { this.parentConcept = parentConcept; }
    public List<Phenomenon> getChildren() { return children; }
    public void setChildren(List<Phenomenon> children) { this.children = children; }
}
