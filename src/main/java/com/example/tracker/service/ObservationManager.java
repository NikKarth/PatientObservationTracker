package com.example.tracker.service;

import com.example.tracker.command.TrackerCommands.RecordCategoryObservationCommand;
import com.example.tracker.command.TrackerCommands.RecordMeasurementCommand;
import com.example.tracker.command.TrackerCommands.RejectObservationCommand;
import com.example.tracker.diagnosis.DiagnosisEngine;
import com.example.tracker.diagnosis.EvaluationResult;
import com.example.tracker.event.ObservationSavedEvent;
import com.example.tracker.factory.ObservationFactory;
import com.example.tracker.model.*;
import com.example.tracker.repository.*;
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
    private final ObservationProcessor observationPipeline;
    private final UserRepository userRepository;

    public ObservationManager(ObservationFactory factory,
                              ObservationRepository observationRepository,
                              PatientManager patientManager,
                              PhenomenonTypeRepository phenomenonTypeRepository,
                              PhenomenonRepository phenomenonRepository,
                              ProtocolRepository protocolRepository,
                              CommandLogService commandLogService,
                              ApplicationEventPublisher eventPublisher,
                              DiagnosisEngine diagnosisEngine,
                              ObservationProcessor observationPipeline,
                              UserRepository userRepository) {
        this.factory = factory;
        this.observationRepository = observationRepository;
        this.patientManager = patientManager;
        this.phenomenonTypeRepository = phenomenonTypeRepository;
        this.phenomenonRepository = phenomenonRepository;
        this.protocolRepository = protocolRepository;
        this.commandLogService = commandLogService;
        this.eventPublisher = eventPublisher;
        this.diagnosisEngine = diagnosisEngine;
        this.observationPipeline = observationPipeline;
        this.userRepository = userRepository;
    }

    public Measurement recordMeasurement(Long patientId,
                                         Long phenomenonTypeId,
                                         Double amount,
                                         String unit,
                                         Long protocolId,
                                         Instant applicabilityTime,
                                         String username) {
        Patient patient = patientManager.findPatient(patientId);
        PhenomenonType phenomenonType = phenomenonTypeRepository.findById(phenomenonTypeId)
                .orElseThrow(() -> new IllegalArgumentException("PhenomenonType not found: " + phenomenonTypeId));
        Protocol protocol = protocolId == null ? null : protocolRepository.findById(protocolId)
                .orElseThrow(() -> new IllegalArgumentException("Protocol not found: " + protocolId));
        User user = userRepository.findByUsername(username);
        if (user == null) throw new IllegalArgumentException("User not found");

        ObservationRequest request = new ObservationRequest();
        request.setPatient(patient);
        request.setPhenomenonType(phenomenonType);
        request.setQuantity(new Quantity(amount == null ? null : java.math.BigDecimal.valueOf(amount), unit));
        request.setProtocol(protocol);
        request.setApplicabilityTime(applicabilityTime);
        request.setUser(username);

        ObservationRequest processed = observationPipeline.process(request);

        Measurement measurement = factory.createMeasurement(processed);
        RecordMeasurementCommand command = new RecordMeasurementCommand(observationRepository, commandLogService, eventPublisher, measurement, user);
        Measurement saved = command.execute();
        eventPublisher.publishEvent(new ObservationSavedEvent(this, saved));
        return saved;
    }

    public CategoryObservation recordCategoryObservation(Long patientId,
                                                           Long phenomenonId,
                                                           Presence presence,
                                                           Long protocolId,
                                                           Instant applicabilityTime,
                                                           String username) {
        Patient patient = patientManager.findPatient(patientId);
        Phenomenon phenomenon = phenomenonRepository.findById(phenomenonId)
                .orElseThrow(() -> new IllegalArgumentException("Phenomenon not found: " + phenomenonId));
        Protocol protocol = protocolId == null ? null : protocolRepository.findById(protocolId)
                .orElseThrow(() -> new IllegalArgumentException("Protocol not found: " + protocolId));
        User user = userRepository.findByUsername(username);
        if (user == null) throw new IllegalArgumentException("User not found");

        ObservationRequest request = new ObservationRequest();
        request.setPatient(patient);
        request.setPhenomenon(phenomenon);
        request.setPresence(presence);
        request.setProtocol(protocol);
        request.setApplicabilityTime(applicabilityTime);
        request.setUser(username);

        ObservationRequest processed = observationPipeline.process(request);

        CategoryObservation observation = factory.createCategoryObservation(processed);
        RecordCategoryObservationCommand command = new RecordCategoryObservationCommand(observationRepository, commandLogService, eventPublisher, observation, user);
        CategoryObservation saved = command.execute();
        eventPublisher.publishEvent(new ObservationSavedEvent(this, saved));
        return saved;
    }

    public Observation rejectObservation(Long observationId, String rejectionReason, String username) {
        Observation observation = observationRepository.findById(observationId)
                .orElseThrow(() -> new IllegalArgumentException("Observation not found: " + observationId));
        User user = userRepository.findByUsername(username);
        if (user == null) throw new IllegalArgumentException("User not found");
        String detailedReason = String.format("%s (Rejected by %s [%s])", rejectionReason, user.getUsername(), user.getRole());
        RejectObservationCommand command = new RejectObservationCommand(observationRepository, commandLogService, eventPublisher, observation, detailedReason, user);
        Observation saved = command.execute();
        eventPublisher.publishEvent(new ObservationSavedEvent(this, saved));
        return saved;
    }

    public List<Observation> listObservationsForPatient(Long patientId) {
        Patient patient = patientManager.findPatient(patientId);
        return observationRepository.findByPatientOrderByRecordingTimeDesc(patient);
    }

    public List<EvaluationResult> evaluateRules(Long patientId) {
        Patient patient = patientManager.findPatient(patientId);
        return diagnosisEngine.evaluateRulesForPatient(patient);
    }
}
