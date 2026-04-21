package com.example.tracker.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity
public class PhenomenonType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Enumerated(EnumType.STRING)
    private MeasurementKind kind;

    @ElementCollection
    @CollectionTable(name = "phenomenon_type_allowed_units")
    private Set<String> allowedUnits = new HashSet<>();

    @OneToMany(mappedBy = "phenomenonType", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Phenomenon> phenomena = new HashSet<>();

    private BigDecimal normalMin;
    private BigDecimal normalMax;

    public PhenomenonType() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public MeasurementKind getKind() { return kind; }
    public void setKind(MeasurementKind kind) { this.kind = kind; }
    public Set<String> getAllowedUnits() { return allowedUnits; }
    public void setAllowedUnits(Set<String> allowedUnits) { this.allowedUnits = allowedUnits; }
    public Set<Phenomenon> getPhenomena() { return phenomena; }
    public void setPhenomena(Set<Phenomenon> phenomena) { this.phenomena = phenomena; }
    public BigDecimal getNormalMin() { return normalMin; }
    public void setNormalMin(BigDecimal normalMin) { this.normalMin = normalMin; }
    public BigDecimal getNormalMax() { return normalMax; }
    public void setNormalMax(BigDecimal normalMax) { this.normalMax = normalMax; }
}
