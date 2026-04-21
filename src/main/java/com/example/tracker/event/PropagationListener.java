package com.example.tracker.event;

import com.example.tracker.model.*;
import com.example.tracker.model.enums.Source;
import com.example.tracker.repository.ObservationRepository;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
public class PropagationListener {

    private final ObservationRepository observationRepository;

    public PropagationListener(ObservationRepository observationRepository) {
        this.observationRepository = observationRepository;
    }

    @EventListener
    public void onObservationSaved(ObservationSavedEvent event) {
        Observation obs = event.getObservation();
        if (obs.getStatus() != ObservationStatus.ACTIVE || obs.getSource() != Source.MANUAL) return;

        if (obs instanceof CategoryObservation co) {
            if (co.getPresence() == Presence.PRESENT) {
                propagatePresent(co.getPhenomenon(), obs.getPatient());
            } else {
                propagateAbsent(co.getPhenomenon(), obs.getPatient());
            }
        }
    }

    private void propagatePresent(Phenomenon phen, Patient patient) {
        Phenomenon current = phen.getParentConcept();
        while (current != null) {
            if (!observationRepository.existsByPatientAndPhenomenonAndPresenceAndStatus(patient, current, Presence.PRESENT, ObservationStatus.ACTIVE)) {
                CategoryObservation inferred = new CategoryObservation();
                inferred.setPatient(patient);
                inferred.setPhenomenon(current);
                inferred.setPresence(Presence.PRESENT);
                inferred.setSource(Source.INFERRED);
                inferred.setRecordingTime(Instant.now());
                inferred.setApplicabilityTime(Instant.now());
                observationRepository.save(inferred);
            }
            current = current.getParentConcept();
        }
    }

    private void propagateAbsent(Phenomenon phen, Patient patient) {
        for (Phenomenon child : phen.getChildren()) {
            propagateAbsent(child, patient);
            if (!observationRepository.existsByPatientAndPhenomenonAndPresenceAndStatus(patient, child, Presence.ABSENT, ObservationStatus.ACTIVE)) {
                CategoryObservation inferred = new CategoryObservation();
                inferred.setPatient(patient);
                inferred.setPhenomenon(child);
                inferred.setPresence(Presence.ABSENT);
                inferred.setSource(Source.INFERRED);
                inferred.setRecordingTime(Instant.now());
                inferred.setApplicabilityTime(Instant.now());
                observationRepository.save(inferred);
            }
        }
    }
}