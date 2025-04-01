package com.empfehlo.empfehlungsapp.controllers;

import com.empfehlo.empfehlungsapp.dtos.RecommendationRequestDTO;
import com.empfehlo.empfehlungsapp.dtos.RecommendationResponseDTO;
import com.empfehlo.empfehlungsapp.mappers.RecommendationMapper;
import com.empfehlo.empfehlungsapp.models.Recommendation;
import com.empfehlo.empfehlungsapp.models.User;
import com.empfehlo.empfehlungsapp.repositories.RecommendationRepository;
import com.empfehlo.empfehlungsapp.repositories.UserRepository;
import com.empfehlo.empfehlungsapp.services.PdfGeneratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    private final PdfGeneratorService pdfGeneratorService;

    @Autowired
    private RecommendationRepository recommendationRepository;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    public RecommendationController(PdfGeneratorService pdfGeneratorService) {
        this.pdfGeneratorService = pdfGeneratorService;
    }

    @PostMapping
    public ResponseEntity<?> createRecommendation(@RequestBody RecommendationRequestDTO dto) {
        System.out.println("Empfangener DTO: " + dto);
        System.out.println("userId empfangen: " + dto.getUserId());
        Optional<User> userOpt = userRepository.findById(dto.getUserId());
        System.out.println("Benutzer gefunden? " + userOpt.isPresent());
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("User-ID ungültig!");
        }

        // Erstelle die Empfehlung und speichere sie zunächst ohne PDF-Pfad
        Recommendation recommendation = RecommendationMapper.toEntity(dto, userOpt.get());
        Recommendation saved = recommendationRepository.save(recommendation);

        // Erzeuge das PDF und erhalte den Dateinamen
        try {
            String pdfFilename = pdfGeneratorService.generateAndStorePdf(dto);
            // Aktualisiere die Empfehlung mit dem PDF-Dateipfad
            saved.setDocumentPdfPath(pdfFilename);
            recommendationRepository.save(saved);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("PDF-Generierung fehlgeschlagen");
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(RecommendationMapper.toDTO(saved));
    }




    @GetMapping
    public ResponseEntity<List<RecommendationResponseDTO>> getAll() {
        List<RecommendationResponseDTO> dtos = recommendationRepository.findAll().stream()
                .map(RecommendationMapper::toDTO)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/by-user/{userId}")
    public ResponseEntity<?> getByUser(@PathVariable Long userId) {
        return userRepository.findById(userId)
                .map(user -> {
                    List<RecommendationResponseDTO> dtos = recommendationRepository.findByRecommendedBy(user).stream()
                            .map(RecommendationMapper::toDTO)
                            .toList();
                    return ResponseEntity.ok(dtos);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateRecommendation(@PathVariable Long id, @RequestBody Recommendation recommendationDetails) {
        return recommendationRepository.findById(id)
                .map(existing -> {
                    updateEntity(existing, recommendationDetails);
                    return ResponseEntity.ok(recommendationRepository.save(existing));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    private void updateEntity(Recommendation existing, Recommendation details) {
        existing.setCandidateFirstname(details.getCandidateFirstname());
        existing.setCandidateLastname(details.getCandidateLastname());
        existing.setPosition(details.getPosition());
        existing.setStatus(details.getStatus());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecommendation(@PathVariable Long id) {
        if (recommendationRepository.existsById(id)) {
            recommendationRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}