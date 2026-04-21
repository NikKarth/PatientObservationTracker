package com.example.tracker.repository;

import com.example.tracker.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ObservationRepository extends JpaRepository<Observation, Long> {
    List<Observation> findByPatientOrderByRecordingTimeDesc(Patient patient);
    
    @Query("SELECT COUNT(c) > 0 FROM CategoryObservation c WHERE c.patient = :patient AND c.phenomenon = :phenomenon AND c.presence = :presence AND c.status = :status")
    boolean existsByPatientAndPhenomenonAndPresenceAndStatus(@Param("patient") Patient patient, 
                                                             @Param("phenomenon") Phenomenon phenomenon, 
                                                             @Param("presence") Presence presence, 
                                                             @Param("status") ObservationStatus status);
}
