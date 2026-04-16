package com.example.tracker.repository;

import com.example.tracker.model.CommandLogEntry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommandLogRepository extends JpaRepository<CommandLogEntry, Long> {
}
