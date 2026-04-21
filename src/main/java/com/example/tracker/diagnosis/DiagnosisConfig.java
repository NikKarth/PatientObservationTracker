package com.example.tracker.diagnosis;

import com.example.tracker.model.enums.StrategyType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.Map;

@Configuration
public class DiagnosisConfig {

    @Bean
    public Map<StrategyType, DiagnosisStrategy> diagnosisStrategies(SimpleConjunctiveStrategy conjunctive, WeightedScoringStrategy weighted) {
        return Map.of(
            StrategyType.CONJUNCTIVE, conjunctive,
            StrategyType.WEIGHTED, weighted
        );
    }

}