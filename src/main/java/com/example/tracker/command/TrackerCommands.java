package com.example.tracker.command;

import com.example.tracker.event.ObservationSavedEvent;
import com.example.tracker.model.*;
import com.example.tracker.repository.ObservationRepository;
import com.example.tracker.repository.PatientRepository;
import com.example.tracker.service.CommandLogService;
import org.springframework.context.ApplicationEventPublisher;

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
        private final ApplicationEventPublisher eventPublisher;
        private final Measurement measurement;
        private final User user;
        private Long observationId;

        public RecordMeasurementCommand(ObservationRepository repository,
                                        CommandLogService logService,
                                        ApplicationEventPublisher eventPublisher,
                                        Measurement measurement,
                                        User user) {
            this.repository = repository;
            this.logService = logService;
            this.eventPublisher = eventPublisher;
            this.measurement = measurement;
            this.user = user;
            this.observationId = measurement.getId();
        }

        @Override
        public Measurement execute() {
            Measurement saved = repository.save(measurement);
            this.observationId = saved.getId();
            logService.logCommand("RecordMeasurementCommand", new MeasurementPayload(saved), user);
            return saved;
        }

        @Override
        public void undo() throws UnsupportedOperationException {
            Long id = observationId != null ? observationId : measurement.getId();
            Measurement saved = (Measurement) repository.findById(id).orElseThrow();
            saved.setStatus(ObservationStatus.REJECTED);
            saved.setRejectionReason("Undone by user");
            Measurement rejected = repository.save(saved);
            eventPublisher.publishEvent(new ObservationSavedEvent(this, rejected));
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
        private final ApplicationEventPublisher eventPublisher;
        private final CategoryObservation observation;
        private final User user;
        private Long observationId;

        public RecordCategoryObservationCommand(ObservationRepository repository,
                                                CommandLogService logService,
                                                ApplicationEventPublisher eventPublisher,
                                                CategoryObservation observation,
                                                User user) {
            this.repository = repository;
            this.logService = logService;
            this.eventPublisher = eventPublisher;
            this.observation = observation;
            this.user = user;
            this.observationId = observation.getId();
        }

        @Override
        public CategoryObservation execute() {
            CategoryObservation saved = repository.save(observation);
            this.observationId = saved.getId();
            logService.logCommand("RecordCategoryObservationCommand", new CategoryObservationPayload(saved), user);
            return saved;
        }

        @Override
        public void undo() throws UnsupportedOperationException {
            Long id = observationId != null ? observationId : observation.getId();
            CategoryObservation saved = (CategoryObservation) repository.findById(id).orElseThrow();
            saved.setStatus(ObservationStatus.REJECTED);
            saved.setRejectionReason("Undone by user");
            CategoryObservation rejected = repository.save(saved);
            eventPublisher.publishEvent(new ObservationSavedEvent(this, rejected));
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
        private final ApplicationEventPublisher eventPublisher;
        private final Observation observation;
        private final String rejectionReason;
        private final User user;

        public RejectObservationCommand(ObservationRepository repository,
                                        CommandLogService logService,
                                        ApplicationEventPublisher eventPublisher,
                                        Observation observation,
                                        String rejectionReason,
                                        User user) {
            this.repository = repository;
            this.logService = logService;
            this.eventPublisher = eventPublisher;
            this.observation = observation;
            this.rejectionReason = rejectionReason;
            this.user = user;
        }

        @Override
        public Observation execute() {
            observation.setStatus(ObservationStatus.REJECTED);
            observation.setRejectionReason(rejectionReason);
            observation.setRejectedBy(user);
            Observation saved = repository.save(observation);
            logService.logCommand("RejectObservationCommand", new RejectObservationPayload(saved), user);
            return saved;
        }

        @Override
        public void undo() throws UnsupportedOperationException {
            observation.setStatus(ObservationStatus.ACTIVE);
            observation.setRejectionReason(null);
            observation.setRejectedBy(null);
            Observation restored = repository.save(observation);
            eventPublisher.publishEvent(new ObservationSavedEvent(this, restored));
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
