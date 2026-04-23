package com.example.tracker.service;

import com.example.tracker.model.*;
import com.example.tracker.model.enums.StrategyType;
import com.example.tracker.repository.AssociativeFunctionRepository;
import com.example.tracker.repository.PhenomenonRepository;
import com.example.tracker.repository.PhenomenonTypeRepository;
import com.example.tracker.repository.ProtocolRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class CatalogManager {

    private final PhenomenonTypeRepository phenomenonTypeRepository;
    private final PhenomenonRepository phenomenonRepository;
    private final ProtocolRepository protocolRepository;
    private final AssociativeFunctionRepository associativeFunctionRepository;

    public CatalogManager(PhenomenonTypeRepository phenomenonTypeRepository,
                          PhenomenonRepository phenomenonRepository,
                          ProtocolRepository protocolRepository,
                          AssociativeFunctionRepository associativeFunctionRepository) {
        this.phenomenonTypeRepository = phenomenonTypeRepository;
        this.phenomenonRepository = phenomenonRepository;
        this.protocolRepository = protocolRepository;
        this.associativeFunctionRepository = associativeFunctionRepository;
    }

    public PhenomenonType createPhenomenonType(String name,
                                               MeasurementKind kind,
                                               Set<String> allowedUnits,
                                               List<Object> phenomenaList,
                                               java.math.BigDecimal normalMin,
                                               java.math.BigDecimal normalMax) {
        PhenomenonType type = new PhenomenonType();
        type.setName(name);
        type.setKind(kind);
        if (allowedUnits != null) {
            type.setAllowedUnits(new HashSet<>(allowedUnits));
        }
        if (normalMin != null) {
            type.setNormalMin(normalMin);
        }
        if (normalMax != null) {
            type.setNormalMax(normalMax);
        }
        if (kind == MeasurementKind.QUALITATIVE && phenomenaList != null) {
            for (Object phenomenonObj : phenomenaList) {
                Phenomenon phenomenon = new Phenomenon();
                String phenomenonName = null;
                Long parentConceptId = null;
                
                if (phenomenonObj instanceof String) {
                    phenomenonName = (String) phenomenonObj;
                } else if (phenomenonObj instanceof java.util.Map) {
                    java.util.Map<String, Object> map = (java.util.Map<String, Object>) phenomenonObj;
                    phenomenonName = (String) map.get("name");
                    Object parentId = map.get("parentConceptId");
                    if (parentId != null) {
                        if (parentId instanceof Number) {
                            parentConceptId = ((Number) parentId).longValue();
                        } else if (parentId instanceof String) {
                            try {
                                parentConceptId = Long.parseLong((String) parentId);
                            } catch (NumberFormatException e) {
                                // parentConceptId remains null
                            }
                        }
                    }
                }
                
                if (phenomenonName != null) {
                    phenomenon.setName(phenomenonName);
                    phenomenon.setPhenomenonType(type);
                    if (parentConceptId != null) {
                        Phenomenon parentConcept = phenomenonRepository.findById(parentConceptId).orElse(null);
                        if (parentConcept != null) {
                            phenomenon.setParentConcept(parentConcept);
                        }
                    }
                    type.getPhenomena().add(phenomenon);
                }
            }
        }
        return phenomenonTypeRepository.save(type);
    }

    public List<PhenomenonType> listPhenomenonTypes() {
        return phenomenonTypeRepository.findAll();
    }

    public List<Phenomenon> listPhenomena() {
        return phenomenonRepository.findAll();
    }

    public Phenomenon createPhenomenon(String name, Long phenomenonTypeId, Long parentConceptId) {
        Phenomenon phenomenon = new Phenomenon();
        phenomenon.setName(name);
        PhenomenonType type = phenomenonTypeRepository.findById(phenomenonTypeId)
                .orElseThrow(() -> new IllegalArgumentException("PhenomenonType not found: " + phenomenonTypeId));
        phenomenon.setPhenomenonType(type);
        if (parentConceptId != null) {
            Phenomenon parentConcept = phenomenonRepository.findById(parentConceptId)
                    .orElseThrow(() -> new IllegalArgumentException("Parent concept not found: " + parentConceptId));
            phenomenon.setParentConcept(parentConcept);
        }
        return phenomenonRepository.save(phenomenon);
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

    public AssociativeFunction createAssociativeFunction(String name, List<String> argumentPairs, String productConcept, StrategyType strategyType, double threshold) {
        AssociativeFunction af = new AssociativeFunction();
        af.setName(name);
        List<ArgumentWeight> arguments = new ArrayList<>();
        for (String pair : argumentPairs) {
            String[] parts = pair.split(":");
            if (parts.length == 2) {
                arguments.add(new ArgumentWeight(parts[0].trim(), Double.parseDouble(parts[1].trim())));
            }
        }
        af.setArguments(arguments);
        af.setProductConcept(productConcept);
        af.setStrategyType(strategyType);
        af.setThreshold(threshold);
        return associativeFunctionRepository.save(af);
    }

    public List<AssociativeFunction> listAssociativeFunctions() {
        return associativeFunctionRepository.findAll();
    }
}
