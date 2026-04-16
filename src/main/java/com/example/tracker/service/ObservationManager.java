package com.example.tracker.service;

import com.example.tracker.command.TrackerCommands.RecordCategoryObservationCommand;
import com.example.tracker.command.TrackerCommands.RecordMeasurementCommand;
import com.example.tracker.command.TrackerCommands.RejectObservationCommand;
import com.example.tracker.event.ObservationSavedEvent;
import com.example.tracker.factory.ObservationFactory;
import com.example.tracker.model.*;
import com.example.tracker.repository.ObservationRepository;
import com.example.tracker.repository.PhenomenonRepository;
import com.example.tracker.repository.PhenomenonTypeRepository;
import com.example.tracker.repository.ProtocolRepository;
import com.example.tracker.diagnosis.DiagnosisEngine;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class ObservationManager {

    private final ObservationFactory factory;
    private final ObservationRepository observationRepository;
    private final PatientManager patientManager;
    private final PhenomenonTypeRepository phenomenonTypeRepository;
    private final PhenomenonRepository phenomenonRepository;
    private final ProtocolRepository protocolRepository;
    private final CommandLogService commandLogService;
    private final ApplicationEventPublisher eventPublisher;
    private final DiagnosisEngine diagnosisEngine;

    public ObservationManager(ObservationFactory factory,
                              ObservationRepository observationRepository,
                              PatientManager patientManager,
                              PhenomenonTypeRepository phenomenonTypeRepository,
                              PhenomenonRepository phenomenonRepository,
                              ProtocolRepository protocolRepository,
                              CommandLogService commandLogService,
                              ApplicationEventPublisher eventPublisher,
                              DiagnosisEngine diagnosisEngine) {
        this.factory = factory;
        this.observationRepository = observationRepository;
        this.patientManager = patientManager;
        this.phenomenonTypeRepository = phenomenonTypeRepository;
        this.phenomenonRepository = phenomenonRepository;
        this.protocolRepository = protocolRepository;
        this.commandLogService = commandLogService;
        this.eventPublisher = eventPublisher;
        this.diagnosisEngine = diagnosisEngine;
    }

    public Measurement recordMeasurement(Long patientId,
                                         Long phenomenonTypeId,
                                         Double amount,
                                         String unit,
                                         Long protocolId,
                                         Instant applicabilityTime) {
        Patient patient = patientManager.findPatient(patientId);
        PhenomenonType phenomenonType = phenomenonTypeRepository.findById(phenomenonTypeId)
                .orElseThrow(() -> new IllegalArgumentException("PhenomenonType not found: " + phenomenonTypeId));
        Protocol protocol = protocolId == null ? null : protocolRepository.findById(protocolId)
                .orElseThrow(() -> new IllegalArgumentException("Protocol not found: " + protocolId));

        Measurement measurement = factory.createMeasurement(patient, phenomenonType, amount, unit, protocol, applicabilityTime);
        RecordMeasurementCommand command = new RecordMeasurementCommand(observationRepository, commandLogService, measurement);
        Measurement saved = command.execute();
        eventPublisher.publishEvent(new ObservationSavedEvent(this, saved));
        return saved;
    }

    public CategoryObservation recordCategoryObservation(Long patientId,
                                                           Long phenomenonId,
                                                           Presence presence,
                                                           Long protocolId,
                                                           Instant applicabilityTime) {
        Patient patient = patientManager.findPatient(patientId);
        Phenomenon phenomenon = phenomenonRepository.findById(phenomenonId)
                .orElseThrow(() -> new IllegalArgumentException("Phenomenon not found: " + phenomenonId));
        Protocol protocol = protocolId == null ? null : protocolRepository.findById(protocolId)
                .orElseThrow(() -> new IllegalArgumentException("Protocol not found: " + protocolId));

        CategoryObservation observation = factory.createCategoryObservation(patient, phenomenon, presence, protocol, applicabilityTime);
        RecordCategoryObservationCommand command = new RecordCategoryObservationCommand(observationRepository, commandLogService, observation);
        CategoryObservation saved = command.execute();
        eventPublisher.publishEvent(new ObservationSavedEvent(this, saved));
        return saved;
    }

    public Observation rejectObservation(Long observationId, String rejectionReason) {
        Observation observation = observationRepository.findById(observationId)
                .orElseThrow(() -> new IllegalArgumentException("Observation not found: " + observationId));
        RejectObservationCommand command = new RejectObservationCommand(observationRepository, commandLogService, observation, rejectionReason);
        Observation saved = command.execute();
        eventPublisher.publishEvent(new ObservationSavedEvent(this, saved));
        return saved;
    }

    public List<Observation> listObservationsForPatient(Long patientId) {
        Patient patient = patientManager.findPatient(patientId);
        return observationRepository.findByPatientOrderByRecordingTimeDesc(patient);
    }

    public List<String> evaluateRules(Long patientId) {
        Patient patient = patientManager.findPatient(patientId);
        return diagnosisEngine.evaluateRulesForPatient(patient);
    }
}
