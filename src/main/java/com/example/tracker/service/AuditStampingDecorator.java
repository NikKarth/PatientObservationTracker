package com.example.tracker.service;

import java.time.Clock;
import java.time.Instant;

public class AuditStampingDecorator extends ObservationProcessorDecorator {

    private final Clock clock;

    public AuditStampingDecorator(ObservationProcessor delegate, Clock clock) {
        super(delegate);
        this.clock = clock;
    }

    @Override
    public ObservationRequest process(ObservationRequest request) {
        request.setRecordingTime(Instant.now(clock));
        return super.process(request);
    }

}