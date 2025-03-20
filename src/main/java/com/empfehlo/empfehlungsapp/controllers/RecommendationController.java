package com.empfehlo.empfehlungsapp.controllers;

import com.empfehlo.empfehlungsapp.models.Recommendation;
import com.empfehlo.empfehlungsapp.repositories.RecommendationRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    @Autowired
    private RecommendationRepository recommendationRepository;

    private static final String UPLOAD_DIR = "uploads/";

    // POST-Endpunkt zum Erstellen einer neuen Empfehlung
    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<Recommendation> createRecommendation(
            @RequestParam("recommendation") String recommendationJson,
            @RequestPart(value = "cv", required = false) MultipartFile cv,
            @RequestPart(value = "coverLetter", required = false) MultipartFile coverLetter) {

        // JSON-String in Java-Objekt umwandeln
        ObjectMapper objectMapper = new ObjectMapper();
        Recommendation recommendation;
        try {
            recommendation = objectMapper.readValue(recommendationJson, Recommendation.class);
        } catch (JsonProcessingException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

        recommendation.setSubmittedAt(LocalDateTime.now());
        recommendation.setStatus("Eingereicht");

        try {
            if (cv != null && !cv.isEmpty()) {
                String cvFileName = saveFile(cv);
                recommendation.setDocumentCvPath(cvFileName);
            }
            if (coverLetter != null && !coverLetter.isEmpty()) {
                String coverLetterFileName = saveFile(coverLetter);
                recommendation.setDocumentCoverLetterPath(coverLetterFileName);
            }
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        Recommendation savedRecommendation = recommendationRepository.save(recommendation);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedRecommendation);
    }


    // Hilfsmethode zum Speichern von Dateien
    private String saveFile(MultipartFile file) throws IOException {
        File directory = new File(UPLOAD_DIR);
        if (!directory.exists()) {
            directory.mkdir();
        }
        String filePath = UPLOAD_DIR + System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path path = Paths.get(filePath);
        Files.write(path, file.getBytes());
        return filePath;
    }

    // GET-Endpunkt für alle Empfehlungen
    @GetMapping
    public ResponseEntity<List<Recommendation>> getAllRecommendations() {
        return ResponseEntity.ok(recommendationRepository.findAll());
    }

    // GET-Endpunkt für einzelne Empfehlung nach ID
    @GetMapping("/{id}")
    public ResponseEntity<Recommendation> getRecommendationById(@PathVariable Long id) {
        Optional<Recommendation> recommendation = recommendationRepository.findById(id);
        return recommendation
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Recommendation> updateRecommendation(@PathVariable Long id, @RequestBody Recommendation recommendationDetails) {
        Optional<Recommendation> optionalRecommendation = recommendationRepository.findById(id);
        if (optionalRecommendation.isPresent()) {
            Recommendation recommendation = optionalRecommendation.get();
            recommendation.setCandidateFirstname(recommendationDetails.getCandidateFirstname());
            recommendation.setCandidateLastname(recommendationDetails.getCandidateLastname());
            recommendation.setPosition(recommendationDetails.getPosition());
            recommendation.setStatus(recommendationDetails.getStatus());

            recommendationRepository.save(recommendation);
            return ResponseEntity.ok(recommendation);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecommendation(@PathVariable Long id) {
        if (recommendationRepository.existsById(id)) {
            recommendationRepository.deleteById(id);
            return ResponseEntity.noContent().build(); // 204 No Content
        } else {
            return ResponseEntity.notFound().build(); // 404 Not Found
        }
    }


}

