package com.example.tracker.event;

import com.example.tracker.model.Observation;
import org.springframework.context.ApplicationEvent;

public class ObservationSavedEvent extends ApplicationEvent {

    private final Observation observation;

    public ObservationSavedEvent(Object source, Observation observation) {
        super(source);
        this.observation = observation;
    }

    public Observation getObservation() {
        return observation;
    }
}
