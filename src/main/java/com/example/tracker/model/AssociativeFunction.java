package com.example.tracker.model;

import com.example.tracker.model.enums.StrategyType;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class AssociativeFunction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ElementCollection
    @CollectionTable(name = "associative_arguments")
    private List<ArgumentWeight> arguments = new ArrayList<>();

    private String productConcept;

    private double threshold;

    @Enumerated(EnumType.STRING)
    private StrategyType strategyType = StrategyType.CONJUNCTIVE;

    public AssociativeFunction() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public List<ArgumentWeight> getArguments() { return arguments; }
    public void setArguments(List<ArgumentWeight> arguments) { this.arguments = arguments; }
    public String getProductConcept() { return productConcept; }
    public void setProductConcept(String productConcept) { this.productConcept = productConcept; }
    public double getThreshold() { return threshold; }
    public void setThreshold(double threshold) { this.threshold = threshold; }
    public StrategyType getStrategyType() { return strategyType; }
    public void setStrategyType(StrategyType strategyType) { this.strategyType = strategyType; }
}
