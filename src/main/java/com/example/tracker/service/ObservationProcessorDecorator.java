package com.example.tracker.service;

public abstract class ObservationProcessorDecorator implements ObservationProcessor {

    private final ObservationProcessor delegate;

    protected ObservationProcessorDecorator(ObservationProcessor delegate) {
        this.delegate = delegate;
    }

    @Override
    public ObservationRequest process(ObservationRequest request) {
        return delegate.process(request);
    }

}