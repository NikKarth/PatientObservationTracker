package com.example.tracker.service;

public class BaseObservationProcessor implements ObservationProcessor {

    @Override
    public ObservationRequest process(ObservationRequest request) {
        return request;
    }

}