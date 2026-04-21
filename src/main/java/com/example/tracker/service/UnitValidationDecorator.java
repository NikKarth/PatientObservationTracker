package com.example.tracker.service;

import com.example.tracker.model.Quantity;
import jakarta.validation.ValidationException;

public class UnitValidationDecorator extends ObservationProcessorDecorator {

    public UnitValidationDecorator(ObservationProcessor delegate) {
        super(delegate);
    }

    @Override
    public ObservationRequest process(ObservationRequest request) {
        Quantity quantity = request.getQuantity();
        if (quantity != null) {
            if (!request.getPhenomenonType().getAllowedUnits().contains(quantity.getUnit())) {
                throw new ValidationException("Invalid unit for phenomenon type");
            }
        }
        return super.process(request);
    }

}