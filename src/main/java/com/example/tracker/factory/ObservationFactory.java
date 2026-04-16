package com.example.tracker.factory;

import com.example.tracker.model.*;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;

@Service
public class ObservationFactory {

    private final Clock clock;

    @org.springframework.beans.factory.annotation.Autowired(required = false)
    public ObservationFactory(Clock clock) {
        this.clock = clock == null ? Clock.systemUTC() : clock;
    }

    public Measurement createMeasurement(Patient patient,
                                         PhenomenonType phenomenonType,
                                         Double amount,
                                         String unit,
                                         Protocol protocol,
                                         Instant applicabilityTime) {
        // validation
        if (phenomenonType.getKind() != MeasurementKind.QUANTITATIVE) {
            throw new ObservationValidationException("PhenomenonType is not quantitative");
        }
        if (unit == null || !phenomenonType.getAllowedUnits().contains(unit)) {
            throw new ObservationValidationException("Unit is not allowed for this phenomenon type");
        }
        Measurement m = new Measurement();
        m.setPatient(patient);
        m.setPhenomenonType(phenomenonType);
        java.math.BigDecimal amt = amount == null ? null : java.math.BigDecimal.valueOf(amount);
        m.setQuantity(new Quantity(amt, unit));
        m.setProtocol(protocol);
        Instant now = Instant.now(clock);
        m.setRecordingTime(now);
        m.setApplicabilityTime(applicabilityTime == null ? now : applicabilityTime);
        return m;
    }

    public CategoryObservation createCategoryObservation(Patient patient,
                                                         Phenomenon phenomenon,
                                                         Presence presence,
                                                         Protocol protocol,
                                                         Instant applicabilityTime) {
        // validation
        if (phenomenon.getPhenomenonType().getKind() != MeasurementKind.QUALITATIVE) {
            throw new ObservationValidationException("Phenomenon is not qualitative");
        }
        CategoryObservation c = new CategoryObservation();
        c.setPatient(patient);
        c.setPhenomenon(phenomenon);
        c.setPresence(presence);
        c.setProtocol(protocol);
        Instant now = Instant.now(clock);
        c.setRecordingTime(now);
        c.setApplicabilityTime(applicabilityTime == null ? now : applicabilityTime);
        return c;
    }

    public static class ObservationValidationException extends RuntimeException {
        public ObservationValidationException(String message) {
            super(message);
        }
    }
}
