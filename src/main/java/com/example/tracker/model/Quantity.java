package com.example.tracker.model;

import jakarta.persistence.Embeddable;
import java.math.BigDecimal;

@Embeddable
public class Quantity {

    private BigDecimal amount;
    private String unit;

    public Quantity() {}

    public Quantity(BigDecimal amount, String unit) {
        this.amount = amount;
        this.unit = unit;
    }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
}
