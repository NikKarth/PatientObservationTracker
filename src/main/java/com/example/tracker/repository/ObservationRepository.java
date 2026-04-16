package com.example.tracker.repository;

import com.example.tracker.model.Observation;
import com.example.tracker.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ObservationRepository extends JpaRepository<Observation, Long> {
    List<Observation> findByPatientOrderByRecordingTimeDesc(Patient patient);
}
