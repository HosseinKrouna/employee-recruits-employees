package com.empfehlo.empfehlungsapp.controllers;

import com.empfehlo.empfehlungsapp.models.Recommendation;
import com.empfehlo.empfehlungsapp.repositories.RecommendationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    @Autowired
    private RecommendationRepository recommendationRepository;

    @GetMapping
    public List<Recommendation> getAllRecommendations() {
        return recommendationRepository.findAll();
    }

    @PostMapping
    public Recommendation createRecommendation(@RequestBody Recommendation recommendation) {
        recommendation.setSubmittedAt(java.time.LocalDateTime.now());
        recommendation.setStatus("In Bearbeitung");
        return recommendationRepository.save(recommendation);
    }
}
