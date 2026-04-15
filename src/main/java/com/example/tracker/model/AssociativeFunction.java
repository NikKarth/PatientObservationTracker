package com.example.tracker.model;

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
    @CollectionTable(name = "associative_args")
    private List<String> argumentConcepts = new ArrayList<>();

    private String productConcept;

    public AssociativeFunction() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public List<String> getArgumentConcepts() { return argumentConcepts; }
    public void setArgumentConcepts(List<String> argumentConcepts) { this.argumentConcepts = argumentConcepts; }
    public String getProductConcept() { return productConcept; }
    public void setProductConcept(String productConcept) { this.productConcept = productConcept; }
}
