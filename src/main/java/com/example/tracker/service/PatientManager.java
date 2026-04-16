package com.example.tracker.service;

import com.example.tracker.command.TrackerCommands.CreatePatientCommand;
import com.example.tracker.model.Patient;
import com.example.tracker.repository.PatientRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class PatientManager {

    private final PatientRepository patientRepository;
    private final CommandLogService commandLogService;

    public PatientManager(PatientRepository patientRepository, CommandLogService commandLogService) {
        this.patientRepository = patientRepository;
        this.commandLogService = commandLogService;
    }

    public Patient createPatient(String fullName, LocalDate dateOfBirth, String note) {
        Patient patient = new Patient();
        patient.setFullName(fullName);
        patient.setDateOfBirth(dateOfBirth);
        patient.setNote(note);
        CreatePatientCommand command = new CreatePatientCommand(patientRepository, commandLogService, patient);
        return command.execute();
    }

    public List<Patient> listPatients() {
        return patientRepository.findAll();
    }

    public Patient findPatient(Long patientId) {
        return patientRepository.findById(patientId)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found: " + patientId));
    }
}
