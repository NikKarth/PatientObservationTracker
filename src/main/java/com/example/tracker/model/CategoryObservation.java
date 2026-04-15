package com.example.tracker.model;

import jakarta.persistence.*;

@Entity
public class CategoryObservation extends Observation {

    @ManyToOne(optional = false)
    private Phenomenon phenomenon;

    @Enumerated(EnumType.STRING)
    private Presence presence;

    public CategoryObservation() {}

    public Phenomenon getPhenomenon() { return phenomenon; }
    public void setPhenomenon(Phenomenon phenomenon) { this.phenomenon = phenomenon; }
    public Presence getPresence() { return presence; }
    public void setPresence(Presence presence) { this.presence = presence; }
}
