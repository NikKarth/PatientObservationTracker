package com.example.tracker.model;

import jakarta.persistence.Embeddable;

@Embeddable
public class ArgumentWeight {

    private String concept;
    private double weight;

    public ArgumentWeight() {}

    public ArgumentWeight(String concept, double weight) {
        this.concept = concept;
        this.weight = weight;
    }

    public String getConcept() { return concept; }
    public void setConcept(String concept) { this.concept = concept; }
    public double getWeight() { return weight; }
    public void setWeight(double weight) { this.weight = weight; }
}