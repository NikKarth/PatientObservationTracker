package com.example.tracker.repository;

import com.example.tracker.model.Protocol;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProtocolRepository extends JpaRepository<Protocol, Long> {
}
