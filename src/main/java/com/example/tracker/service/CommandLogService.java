package com.example.tracker.service;

import com.example.tracker.model.CommandLogEntry;
import com.example.tracker.repository.CommandLogRepository;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

@Service
public class CommandLogService {

    private static final String DEFAULT_USER = "staff";

    private final CommandLogRepository repository;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    public CommandLogService(CommandLogRepository repository, ObjectMapper objectMapper, Clock clock) {
        this.repository = repository;
        this.objectMapper = objectMapper;
        this.clock = clock;
    }

    public void logCommand(String commandType, Object payload) {
        repository.save(new CommandLogEntry(commandType, serializePayload(payload), Instant.now(clock), DEFAULT_USER));
    }

    public List<CommandLogEntry> listCommands() {
        return repository.findAll();
    }

    private String serializePayload(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JacksonException e) {
            return String.format("{\"error\": \"Unable to serialize payload: %s\"}", e.getMessage());
        }
    }
}
