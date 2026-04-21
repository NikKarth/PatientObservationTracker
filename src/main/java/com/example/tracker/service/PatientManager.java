package com.example.tracker.service;

import com.example.tracker.command.TrackerCommands.CreatePatientCommand;
import com.example.tracker.model.Patient;
import com.example.tracker.model.User;
import com.example.tracker.repository.PatientRepository;
import com.example.tracker.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class PatientManager {

    private final PatientRepository patientRepository;
    private final CommandLogService commandLogService;
    private final UserRepository userRepository;

    public PatientManager(PatientRepository patientRepository, CommandLogService commandLogService, UserRepository userRepository) {
        this.patientRepository = patientRepository;
        this.commandLogService = commandLogService;
        this.userRepository = userRepository;
    }

    public Patient createPatient(String fullName, LocalDate dateOfBirth, String note, String username) {
        Patient patient = new Patient();
        patient.setFullName(fullName);
        patient.setDateOfBirth(dateOfBirth);
        patient.setNote(note);
        User user = userRepository.findByUsername(username);
        if (user == null) throw new IllegalArgumentException("User not found");
        CreatePatientCommand command = new CreatePatientCommand(patientRepository, commandLogService, patient, user);
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
