package com.example.tracker.service;

import com.example.tracker.model.*;
import com.example.tracker.repository.CommandLogRepository;
import com.example.tracker.repository.ObservationRepository;
import com.example.tracker.repository.PatientRepository;
import com.example.tracker.repository.UserRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

@Service
public class CommandLogService {

    private final CommandLogRepository repository;
    private final ObservationRepository observationRepository;
    private final PatientRepository patientRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final Clock clock;
    private final ApplicationEventPublisher eventPublisher;

    public CommandLogService(CommandLogRepository repository, ObservationRepository observationRepository,
                             PatientRepository patientRepository, UserRepository userRepository,
                             ObjectMapper objectMapper, Clock clock, ApplicationEventPublisher eventPublisher) {
        this.repository = repository;
        this.observationRepository = observationRepository;
        this.patientRepository = patientRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
        this.clock = clock;
        this.eventPublisher = eventPublisher;
    }

    public void logCommand(String commandType, Object payload, User user) {
        repository.save(new CommandLogEntry(commandType, serializePayload(payload), Instant.now(clock), user));
    }

    public List<CommandLogEntry> listCommands() {
        return repository.findAll();
    }

    public void undoCommand(Long commandId, User currentUser) {
        CommandLogEntry entry = repository.findById(commandId).orElseThrow(() -> new IllegalArgumentException("Command not found"));
        if (entry.isUndone()) {
            throw new IllegalStateException("Command already undone");
        }
        if (entry.getUser() == null || currentUser == null || entry.getUser().getUsername() == null || currentUser.getUsername() == null
                || !entry.getUser().getUsername().equals(currentUser.getUsername())) {
            throw new IllegalArgumentException("Not authorized to undo this command");
        }
        switch (entry.getCommandType()) {
            case "CreatePatientCommand" -> throw new UnsupportedOperationException("Cannot undo patient creation");
            case "RecordMeasurementCommand" -> undoRecordMeasurement(entry);
            case "RecordCategoryObservationCommand" -> undoRecordCategoryObservation(entry);
            case "RejectObservationCommand" -> undoRejectObservation(entry);
            default -> throw new IllegalArgumentException("Unknown command type");
        }
        entry.setUndone(true);
        repository.save(entry);
    }

    private void undoRecordMeasurement(CommandLogEntry entry) {
        try {
            MeasurementPayload payload = objectMapper.readValue(entry.getPayload(), MeasurementPayload.class);
            Measurement observation = (Measurement) observationRepository.findById(payload.observationId).orElseThrow();
            com.example.tracker.command.TrackerCommands.RecordMeasurementCommand command = new com.example.tracker.command.TrackerCommands.RecordMeasurementCommand(
                    observationRepository,
                    this,
                    eventPublisher,
                    observation,
                    entry.getUser()
            );
            command.undo();
        } catch (Exception e) {
            throw new RuntimeException("Failed to undo", e);
        }
    }

    private void undoRecordCategoryObservation(CommandLogEntry entry) {
        try {
            CategoryObservationPayload payload = objectMapper.readValue(entry.getPayload(), CategoryObservationPayload.class);
            CategoryObservation observation = (CategoryObservation) observationRepository.findById(payload.observationId).orElseThrow();
            com.example.tracker.command.TrackerCommands.RecordCategoryObservationCommand command = new com.example.tracker.command.TrackerCommands.RecordCategoryObservationCommand(
                    observationRepository,
                    this,
                    eventPublisher,
                    observation,
                    entry.getUser()
            );
            command.undo();
        } catch (Exception e) {
            throw new RuntimeException("Failed to undo", e);
        }
    }

    private void undoRejectObservation(CommandLogEntry entry) {
        try {
            RejectObservationPayload payload = objectMapper.readValue(entry.getPayload(), RejectObservationPayload.class);
            Observation observation = observationRepository.findById(payload.observationId).orElseThrow();
            com.example.tracker.command.TrackerCommands.RejectObservationCommand command = new com.example.tracker.command.TrackerCommands.RejectObservationCommand(
                    observationRepository,
                    this,
                    eventPublisher,
                    observation,
                    payload.rejectionReason,
                    entry.getUser()
            );
            command.undo();
        } catch (Exception e) {
            throw new RuntimeException("Failed to undo", e);
        }
    }

    private String serializePayload(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JacksonException e) {
            return String.format("{\"error\": \"Unable to serialize payload: %s\"}", e.getMessage());
        }
    }

    private static class MeasurementPayload {
        public Long observationId;
        public Long patientId;
        public String phenomenonType;
        public Object quantity;
        public String protocolName;
        public Object applicabilityTime;
    }

    private static class CategoryObservationPayload {
        public Long observationId;
        public Long patientId;
        public String phenomenon;
        public String presence;
        public String protocolName;
        public Object applicabilityTime;
    }

    private static class RejectObservationPayload {
        public Long observationId;
        public Long patientId;
        public String rejectionReason;
    }
}
