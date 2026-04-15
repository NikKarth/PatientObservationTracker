package com.example.tracker.repository;

import com.example.tracker.model.Phenomenon;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PhenomenonRepository extends JpaRepository<Phenomenon, Long> {
}
