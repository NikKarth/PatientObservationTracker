
package com.example.tracker.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
public class Measurement extends Observation {

    @ManyToOne(optional = false)
    private PhenomenonType phenomenonType;

    @Embedded
    private Quantity quantity;

    public Measurement() {}

    public PhenomenonType getPhenomenonType() { return phenomenonType; }
    public void setPhenomenonType(PhenomenonType phenomenonType) { this.phenomenonType = phenomenonType; }
    public Quantity getQuantity() { return quantity; }
    public void setQuantity(Quantity quantity) { this.quantity = quantity; }
}
