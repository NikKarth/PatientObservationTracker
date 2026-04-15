package com.example.tracker.repository;

import com.example.tracker.model.Observation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ObservationRepository extends JpaRepository<Observation, Long> {
}
