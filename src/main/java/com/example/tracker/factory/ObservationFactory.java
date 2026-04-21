package com.example.tracker.factory;

import com.example.tracker.model.*;
import com.example.tracker.model.enums.Source;
import com.example.tracker.service.ObservationRequest;
import org.springframework.stereotype.Service;

import java.time.Clock;

@Service
public class ObservationFactory {

    private final Clock clock;

    @org.springframework.beans.factory.annotation.Autowired
    public ObservationFactory(Clock clock) {
        this.clock = clock == null ? Clock.systemUTC() : clock;
    }

    public Measurement createMeasurement(ObservationRequest request) {
        Measurement m = new Measurement();
        m.setPatient(request.getPatient());
        m.setPhenomenonType(request.getPhenomenonType());
        m.setQuantity(request.getQuantity());
        m.setProtocol(request.getProtocol());
        m.setRecordingTime(request.getRecordingTime());
        m.setApplicabilityTime(request.getApplicabilityTime());
        m.setAnomaly(request.isAnomaly());
        m.setSource(Source.MANUAL);
        return m;
    }

    public CategoryObservation createCategoryObservation(ObservationRequest request) {
        CategoryObservation c = new CategoryObservation();
        c.setPatient(request.getPatient());
        c.setPhenomenon(request.getPhenomenon());
        c.setPresence(request.getPresence());
        c.setProtocol(request.getProtocol());
        c.setRecordingTime(request.getRecordingTime());
        c.setApplicabilityTime(request.getApplicabilityTime());
        c.setAnomaly(request.isAnomaly());
        c.setSource(Source.MANUAL);
        return c;
    }
}
