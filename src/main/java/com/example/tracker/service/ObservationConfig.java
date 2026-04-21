package com.example.tracker.service;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.time.Clock;

@Configuration
public class ObservationConfig {

    @Bean
    public ObservationProcessor observationPipeline(Clock clock) {
        return new AuditStampingDecorator(
            new AnomalyFlaggingDecorator(
                new UnitValidationDecorator(
                    new BaseObservationProcessor()
                )
            ), clock
        );
    }

}