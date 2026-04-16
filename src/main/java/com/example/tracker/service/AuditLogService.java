package com.example.tracker.service;

import com.example.tracker.model.AuditLogEntry;
import com.example.tracker.repository.AuditLogRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public List<AuditLogEntry> listAuditEntries() {
        return auditLogRepository.findAll();
    }
}
