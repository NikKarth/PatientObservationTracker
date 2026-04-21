package com.example.tracker.controller;

import com.example.tracker.model.*;
import com.example.tracker.service.AuditLogService;
import com.example.tracker.service.CatalogManager;
import com.example.tracker.service.CommandLogService;
import com.example.tracker.service.ObservationManager;
import com.example.tracker.service.PatientManager;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
public class TrackerController {

    private final PatientManager patientManager;
    private final ObservationManager observationManager;
    private final CatalogManager catalogManager;
    private final CommandLogService commandLogService;
    private final AuditLogService auditLogService;

    public TrackerController(PatientManager patientManager,
                             ObservationManager observationManager,
                             CatalogManager catalogManager,
                             CommandLogService commandLogService,
                             AuditLogService auditLogService) {
        this.patientManager = patientManager;
        this.observationManager = observationManager;
        this.catalogManager = catalogManager;
        this.commandLogService = commandLogService;
        this.auditLogService = auditLogService;
    }

    @GetMapping("/")
    public RedirectView home() {
        return new RedirectView("/index.html");
    }

    @GetMapping("/api/patients")
    public List<PatientDto> listPatients() {
        return patientManager.listPatients().stream()
                .map(PatientDto::fromEntity)
                .collect(Collectors.toList());
    }

    @PostMapping("/api/patients")
    public PatientDto createPatient(@RequestBody CreatePatientRequest request, @RequestHeader("X-User") String username) {
        Patient patient = patientManager.createPatient(request.fullName, request.dateOfBirth, request.note, username);
        return PatientDto.fromEntity(patient);
    }

    @GetMapping("/api/patients/{id}/observations")
    public List<ObservationDto> listObservations(@PathVariable("id") Long id) {
        return observationManager.listObservationsForPatient(id).stream()
                .map(ObservationDto::fromObservation)
                .collect(Collectors.toList());
    }

    @GetMapping("/api/patients/{id}/evaluate")
    public RuleEvaluationResponse evaluateRules(@PathVariable("id") Long id) {
        return new RuleEvaluationResponse(observationManager.evaluateRules(id).stream().map(r -> r.getProductConcept()).collect(Collectors.toList()));
    }

    @PostMapping("/api/observations/measurement")
    public ObservationDto createMeasurement(@RequestBody CreateMeasurementRequest request, @RequestHeader("X-User") String username) {
        Measurement saved = observationManager.recordMeasurement(
                request.patientId,
                request.phenomenonTypeId,
                request.amount,
                request.unit,
                request.protocolId,
                request.applicabilityTime,
                username
        );
        return ObservationDto.fromObservation(saved);
    }

    @PostMapping("/api/observations/category")
    public ObservationDto createCategoryObservation(@RequestBody CreateCategoryObservationRequest request, @RequestHeader("X-User") String username) {
        CategoryObservation saved = observationManager.recordCategoryObservation(
                request.patientId,
                request.phenomenonId,
                request.presence,
                request.protocolId,
                request.applicabilityTime,
                username
        );
        return ObservationDto.fromObservation(saved);
    }

    @PostMapping("/api/observations/{id}/reject")
    public ObservationDto rejectObservation(@PathVariable("id") Long id, @RequestBody RejectObservationRequest request, @RequestHeader("X-User") String username) {
        Observation saved = observationManager.rejectObservation(id, request.reason, username);
        return ObservationDto.fromObservation(saved);
    }

    @GetMapping("/api/phenomenon-types")
    public List<PhenomenonTypeDto> listPhenomenonTypes() {
        return catalogManager.listPhenomenonTypes().stream()
                .map(PhenomenonTypeDto::fromEntity)
                .collect(Collectors.toList());
    }

    @PostMapping("/api/phenomenon-types")
    public PhenomenonTypeDto createPhenomenonType(@RequestBody CreatePhenomenonTypeRequest request) {
        PhenomenonType saved = catalogManager.createPhenomenonType(
                request.name,
                request.kind,
                request.allowedUnits,
                request.phenomena
        );
        return PhenomenonTypeDto.fromEntity(saved);
    }

    @GetMapping("/api/protocols")
    public List<ProtocolDto> listProtocols() {
        return catalogManager.listProtocols().stream()
                .map(ProtocolDto::fromEntity)
                .collect(Collectors.toList());
    }

    @PostMapping("/api/protocols")
    public ProtocolDto createProtocol(@RequestBody CreateProtocolRequest request) {
        Protocol saved = catalogManager.createProtocol(request.name, request.description, request.accuracyRating);
        return ProtocolDto.fromEntity(saved);
    }

    @GetMapping("/api/command-log")
    public List<CommandLogDto> listCommands() {
        return commandLogService.listCommands().stream()
                .map(CommandLogDto::fromEntity)
                .collect(Collectors.toList());
    }

    @GetMapping("/api/audit-log")
    public List<AuditLogDto> listAuditLog() {
        return auditLogService.listAuditEntries().stream()
                .map(AuditLogDto::fromEntity)
                .collect(Collectors.toList());
    }

    public static class PatientDto {
        public Long id;
        public String fullName;
        public LocalDate dateOfBirth;
        public String note;

        public static PatientDto fromEntity(Patient patient) {
            PatientDto dto = new PatientDto();
            dto.id = patient.getId();
            dto.fullName = patient.getFullName();
            dto.dateOfBirth = patient.getDateOfBirth();
            dto.note = patient.getNote();
            return dto;
        }
    }

    public static class CreatePatientRequest {
        public String fullName;
        public LocalDate dateOfBirth;
        public String note;
    }

    public static class ObservationDto {
        public Long id;
        public String type;
        public Long patientId;
        public String patientName;
        public String phenomenonType;
        public Double amount;
        public String unit;
        public String phenomenon;
        public String presence;
        public String protocol;
        public String status;
        public String rejectionReason;
        public Instant recordingTime;
        public Instant applicabilityTime;

        public static ObservationDto fromObservation(Observation observation) {
            ObservationDto dto = new ObservationDto();
            dto.id = observation.getId();
            dto.patientId = observation.getPatient() != null ? observation.getPatient().getId() : null;
            dto.patientName = observation.getPatient() != null ? observation.getPatient().getFullName() : null;
            dto.status = observation.getStatus() != null ? observation.getStatus().name() : null;
            dto.rejectionReason = observation.getRejectionReason();
            dto.recordingTime = observation.getRecordingTime();
            dto.applicabilityTime = observation.getApplicabilityTime();
            dto.protocol = observation.getProtocol() != null ? observation.getProtocol().getName() : null;
            if (observation instanceof Measurement) {
                Measurement measurement = (Measurement) observation;
                dto.type = "measurement";
                dto.phenomenonType = measurement.getPhenomenonType() != null ? measurement.getPhenomenonType().getName() : null;
                if (measurement.getQuantity() != null) {
                    dto.amount = measurement.getQuantity().getAmount() != null ? measurement.getQuantity().getAmount().doubleValue() : null;
                    dto.unit = measurement.getQuantity().getUnit();
                }
            } else if (observation instanceof CategoryObservation) {
                CategoryObservation categoryObservation = (CategoryObservation) observation;
                dto.type = "category";
                dto.phenomenon = categoryObservation.getPhenomenon() != null ? categoryObservation.getPhenomenon().getName() : null;
                dto.presence = categoryObservation.getPresence() != null ? categoryObservation.getPresence().name() : null;
            }
            return dto;
        }
    }

    public static class CreateMeasurementRequest {
        public Long patientId;
        public Long phenomenonTypeId;
        public Double amount;
        public String unit;
        public Long protocolId;
        public Instant applicabilityTime;
    }

    public static class CreateCategoryObservationRequest {
        public Long patientId;
        public Long phenomenonId;
        public Presence presence;
        public Long protocolId;
        public Instant applicabilityTime;
    }

    public static class RejectObservationRequest {
        public String reason;
    }

    public static class PhenomenonTypeDto {
        public Long id;
        public String name;
        public MeasurementKind kind;
        public Set<String> allowedUnits;
        public List<PhenomenonDto> phenomena;

        public static PhenomenonTypeDto fromEntity(PhenomenonType entity) {
            PhenomenonTypeDto dto = new PhenomenonTypeDto();
            dto.id = entity.getId();
            dto.name = entity.getName();
            dto.kind = entity.getKind();
            dto.allowedUnits = entity.getAllowedUnits();
            dto.phenomena = entity.getPhenomena().stream().map(PhenomenonDto::fromEntity).collect(Collectors.toList());
            return dto;
        }
    }

    public static class PhenomenonDto {
        public Long id;
        public String name;

        public static PhenomenonDto fromEntity(Phenomenon entity) {
            PhenomenonDto dto = new PhenomenonDto();
            dto.id = entity.getId();
            dto.name = entity.getName();
            return dto;
        }
    }

    public static class CreatePhenomenonTypeRequest {
        public String name;
        public MeasurementKind kind;
        public Set<String> allowedUnits;
        public List<String> phenomena;
    }

    public static class ProtocolDto {
        public Long id;
        public String name;
        public String description;
        public AccuracyRating accuracyRating;

        public static ProtocolDto fromEntity(Protocol entity) {
            ProtocolDto dto = new ProtocolDto();
            dto.id = entity.getId();
            dto.name = entity.getName();
            dto.description = entity.getDescription();
            dto.accuracyRating = entity.getAccuracyRating();
            return dto;
        }
    }

    public static class CreateProtocolRequest {
        public String name;
        public String description;
        public AccuracyRating accuracyRating;
    }

    public static class CommandLogDto {
        public Long id;
        public String commandType;
        public String payload;
        public String user;
        public Instant executedAt;

        public static CommandLogDto fromEntity(CommandLogEntry entry) {
            CommandLogDto dto = new CommandLogDto();
            dto.id = entry.getId();
            dto.commandType = entry.getCommandType();
            dto.payload = entry.getPayload();
            dto.user = entry.getUser() != null ? entry.getUser().getUsername() : null;
            dto.executedAt = entry.getExecutedAt();
            return dto;
        }
    }

    public static class AuditLogDto {
        public Long id;
        public Instant timestamp;
        public Long patientId;
        public Long observationId;
        public String event;
        public String details;

        public static AuditLogDto fromEntity(AuditLogEntry entry) {
            AuditLogDto dto = new AuditLogDto();
            dto.id = entry.getId();
            dto.timestamp = entry.getTimestamp();
            dto.patientId = entry.getPatientId();
            dto.observationId = entry.getObservationId();
            dto.event = entry.getEvent();
            dto.details = entry.getDetails();
            return dto;
        }
    }

    public static class RuleEvaluationResponse {
        public List<String> inferences;

        public RuleEvaluationResponse(List<String> inferences) {
            this.inferences = inferences;
        }
    }
}
