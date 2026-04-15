package com.example.tracker.repository;

import com.example.tracker.model.CategoryObservation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryObservationRepository extends JpaRepository<CategoryObservation, Long> {
}
