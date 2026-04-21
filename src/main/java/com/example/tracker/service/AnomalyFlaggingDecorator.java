package com.example.tracker.service;

import com.example.tracker.model.Quantity;
import java.math.BigDecimal;

public class AnomalyFlaggingDecorator extends ObservationProcessorDecorator {

    public AnomalyFlaggingDecorator(ObservationProcessor delegate) {
        super(delegate);
    }

    @Override
    public ObservationRequest process(ObservationRequest request) {
        Quantity quantity = request.getQuantity();
        if (quantity != null && request.getPhenomenonType().getNormalMin() != null) {
            BigDecimal amount = quantity.getAmount();
            if (amount.compareTo(request.getPhenomenonType().getNormalMin()) < 0 ||
                amount.compareTo(request.getPhenomenonType().getNormalMax()) > 0) {
                request.setAnomaly(true);
            }
        }
        return super.process(request);
    }

}