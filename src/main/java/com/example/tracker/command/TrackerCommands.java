package com.example.tracker.command;

import com.example.tracker.model.*;
import com.example.tracker.repository.ObservationRepository;
import com.example.tracker.repository.PatientRepository;
import com.example.tracker.service.CommandLogService;

public class TrackerCommands {

public interface Command<T> {
    T execute();
    void undo() throws UnsupportedOperationException;
}

    public static class CreatePatientCommand implements Command<Patient> {

        private final PatientRepository repository;
        private final CommandLogService logService;
        private final Patient patient;
        private final User user;

        public CreatePatientCommand(PatientRepository repository,
                                    CommandLogService logService,
                                    Patient patient,
                                    User user) {
            this.repository = repository;
            this.logService = logService;
            this.patient = patient;
            this.user = user;
        }

        @Override
        public Patient execute() {
            Patient saved = repository.save(patient);
            logService.logCommand("CreatePatientCommand", new CreatePatientPayload(saved.getFullName(), saved.getDateOfBirth(), saved.getNote()), user);
            return saved;
        }

        @Override
        public void undo() throws UnsupportedOperationException {
            throw new UnsupportedOperationException("Cannot undo patient creation");
        }

        private static class CreatePatientPayload {
            public final String fullName;
            public final Object dateOfBirth;
            public final String note;

            CreatePatientPayload(String fullName, Object dateOfBirth, String note) {
                this.fullName = fullName;
                this.dateOfBirth = dateOfBirth;
                this.note = note;
            }
        }
    }

    public static class RecordMeasurementCommand implements Command<Measurement> {

        private final ObservationRepository repository;
        private final CommandLogService logService;
        private final Measurement measurement;
        private final User user;

        public RecordMeasurementCommand(ObservationRepository repository,
                                        CommandLogService logService,
                                        Measurement measurement,
                                        User user) {
            this.repository = repository;
            this.logService = logService;
            this.measurement = measurement;
            this.user = user;
        }

        @Override
        public Measurement execute() {
            Measurement saved = repository.save(measurement);
            logService.logCommand("RecordMeasurementCommand", new MeasurementPayload(saved), user);
            return saved;
        }

        @Override
        public void undo() throws UnsupportedOperationException {
            // Undo is handled in CommandLogService
            throw new UnsupportedOperationException("Undo via service");
        }

        private static class MeasurementPayload {
            public final Long observationId;
            public final Long patientId;
            public final String phenomenonType;
            public final Object quantity;
            public final String protocolName;
            public final Object applicabilityTime;

            MeasurementPayload(Measurement saved) {
                this.observationId = saved.getId();
                this.patientId = saved.getPatient() != null ? saved.getPatient().getId() : null;
                this.phenomenonType = saved.getPhenomenonType() != null ? saved.getPhenomenonType().getName() : null;
                this.quantity = saved.getQuantity();
                this.protocolName = saved.getProtocol() != null ? saved.getProtocol().getName() : null;
                this.applicabilityTime = saved.getApplicabilityTime();
            }
        }
    }

    public static class RecordCategoryObservationCommand implements Command<CategoryObservation> {

        private final ObservationRepository repository;
        private final CommandLogService logService;
        private final CategoryObservation observation;
        private final User user;

        public RecordCategoryObservationCommand(ObservationRepository repository,
                                                CommandLogService logService,
                                                CategoryObservation observation,
                                                User user) {
            this.repository = repository;
            this.logService = logService;
            this.observation = observation;
            this.user = user;
        }

        @Override
        public CategoryObservation execute() {
            CategoryObservation saved = repository.save(observation);
            logService.logCommand("RecordCategoryObservationCommand", new CategoryObservationPayload(saved), user);
            return saved;
        }

        @Override
        public void undo() throws UnsupportedOperationException {
            throw new UnsupportedOperationException("Undo via service");
        }

        private static class CategoryObservationPayload {
            public final Long observationId;
            public final Long patientId;
            public final String phenomenon;
            public final String presence;
            public final String protocolName;
            public final Object applicabilityTime;

            CategoryObservationPayload(CategoryObservation saved) {
                this.observationId = saved.getId();
                this.patientId = saved.getPatient() != null ? saved.getPatient().getId() : null;
                this.phenomenon = saved.getPhenomenon() != null ? saved.getPhenomenon().getName() : null;
                this.presence = saved.getPresence() != null ? saved.getPresence().name() : null;
                this.protocolName = saved.getProtocol() != null ? saved.getProtocol().getName() : null;
                this.applicabilityTime = saved.getApplicabilityTime();
            }
        }
    }

    public static class RejectObservationCommand implements Command<Observation> {

        private final ObservationRepository repository;
        private final CommandLogService logService;
        private final Observation observation;
        private final String rejectionReason;
        private final User user;

        public RejectObservationCommand(ObservationRepository repository,
                                        CommandLogService logService,
                                        Observation observation,
                                        String rejectionReason,
                                        User user) {
            this.repository = repository;
            this.logService = logService;
            this.observation = observation;
            this.rejectionReason = rejectionReason;
            this.user = user;
        }

        @Override
        public Observation execute() {
            observation.setStatus(com.example.tracker.model.ObservationStatus.REJECTED);
            observation.setRejectionReason(rejectionReason);
            Observation saved = repository.save(observation);
            logService.logCommand("RejectObservationCommand", new RejectObservationPayload(saved), user);
            return saved;
        }

        @Override
        public void undo() throws UnsupportedOperationException {
            throw new UnsupportedOperationException("Undo via service");
        }

        private static class RejectObservationPayload {
            public final Long observationId;
            public final Long patientId;
            public final String rejectionReason;

            RejectObservationPayload(Observation saved) {
                this.observationId = saved.getId();
                this.patientId = saved.getPatient() != null ? saved.getPatient().getId() : null;
                this.rejectionReason = saved.getRejectionReason();
            }
        }
    }
}
