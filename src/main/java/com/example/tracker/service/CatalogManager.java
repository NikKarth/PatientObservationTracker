package com.example.tracker.service;

import com.example.tracker.model.*;
import com.example.tracker.repository.PhenomenonRepository;
import com.example.tracker.repository.PhenomenonTypeRepository;
import com.example.tracker.repository.ProtocolRepository;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class CatalogManager {

    private final PhenomenonTypeRepository phenomenonTypeRepository;
    private final PhenomenonRepository phenomenonRepository;
    private final ProtocolRepository protocolRepository;

    public CatalogManager(PhenomenonTypeRepository phenomenonTypeRepository,
                          PhenomenonRepository phenomenonRepository,
                          ProtocolRepository protocolRepository) {
        this.phenomenonTypeRepository = phenomenonTypeRepository;
        this.phenomenonRepository = phenomenonRepository;
        this.protocolRepository = protocolRepository;
    }

    public PhenomenonType createPhenomenonType(String name,
                                               MeasurementKind kind,
                                               Set<String> allowedUnits,
                                               List<String> phenomenonNames) {
        PhenomenonType type = new PhenomenonType();
        type.setName(name);
        type.setKind(kind);
        if (allowedUnits != null) {
            type.setAllowedUnits(new HashSet<>(allowedUnits));
        }
        if (kind == MeasurementKind.QUALITATIVE && phenomenonNames != null) {
            for (String phenomenonName : phenomenonNames) {
                Phenomenon phenomenon = new Phenomenon();
                phenomenon.setName(phenomenonName);
                phenomenon.setPhenomenonType(type);
                type.getPhenomena().add(phenomenon);
            }
        }
        return phenomenonTypeRepository.save(type);
    }

    public List<PhenomenonType> listPhenomenonTypes() {
        return phenomenonTypeRepository.findAll();
    }

    public Protocol createProtocol(String name, String description, AccuracyRating accuracyRating) {
        Protocol protocol = new Protocol();
        protocol.setName(name);
        protocol.setDescription(description);
        protocol.setAccuracyRating(accuracyRating);
        return protocolRepository.save(protocol);
    }

    public List<Protocol> listProtocols() {
        return protocolRepository.findAll();
    }
}
