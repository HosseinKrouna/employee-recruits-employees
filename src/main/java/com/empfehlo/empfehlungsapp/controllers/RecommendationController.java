package com.empfehlo.empfehlungsapp.controllers;

import com.empfehlo.empfehlungsapp.dtos.RecommendationRequestDTO;
import com.empfehlo.empfehlungsapp.dtos.RecommendationResponseDTO;
import com.empfehlo.empfehlungsapp.mappers.RecommendationMapper;
import com.empfehlo.empfehlungsapp.models.Recommendation;
import com.empfehlo.empfehlungsapp.models.User;
import com.empfehlo.empfehlungsapp.repositories.RecommendationRepository;
import com.empfehlo.empfehlungsapp.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    @Autowired private RecommendationRepository recommendationRepository;
    @Autowired private UserRepository userRepository;

   // ToDo: Refactor
    @PostMapping
    public ResponseEntity<?> createRecommendation(@RequestBody RecommendationRequestDTO dto) {
        System.out.println("Empfangener DTO: " + dto); // <--- Wichtig!
        System.out.println("userId empfangen: " + dto.getUserId());
        Optional<User> userOpt = userRepository.findById(dto.getUserId());
        System.out.println("Benutzer gefunden? " + userOpt.isPresent());
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("User-ID ungültig!");
        }

        Recommendation recommendation = RecommendationMapper.toEntity(dto, userOpt.get());
        Recommendation saved = recommendationRepository.save(recommendation);
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





//package com.empfehlo.empfehlungsapp.controllers;
//
//import com.empfehlo.empfehlungsapp.dtos.RecommendationRequestDTO;
//import com.empfehlo.empfehlungsapp.dtos.RecommendationResponseDTO;
//import com.empfehlo.empfehlungsapp.mappers.RecommendationMapper;
//import com.empfehlo.empfehlungsapp.models.Recommendation;
//import com.empfehlo.empfehlungsapp.models.User;
//import com.empfehlo.empfehlungsapp.repositories.RecommendationRepository;
//import com.empfehlo.empfehlungsapp.repositories.UserRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.Optional;
//
//@RestController
//@RequestMapping("/api/recommendations")
//public class RecommendationController {
//
//    @Autowired
//    private RecommendationRepository recommendationRepository;
//
//    @Autowired
//    private UserRepository userRepository;
//
//    // 📬 POST: Empfehlung erstellen
//    @PostMapping
//    public ResponseEntity<?> createRecommendation(@RequestBody RecommendationRequestDTO dto) {
//        System.out.println("Empfangener DTO: " + dto); // <--- Wichtig!
//        System.out.println("userId empfangen: " + dto.getUserId());
//        Optional<User> userOpt = userRepository.findById(dto.getUserId());
//        System.out.println("Benutzer gefunden? " + userOpt.isPresent());
//        if (userOpt.isEmpty()) {
//            return ResponseEntity.badRequest().body("User-ID ungültig!");
//        }
//
//        Recommendation recommendation = RecommendationMapper.toEntity(dto, userOpt.get());
//        Recommendation saved = recommendationRepository.save(recommendation);
//        return ResponseEntity.status(HttpStatus.CREATED).body(RecommendationMapper.toDTO(saved));
//    }
//
//
//    // 📬 GET: Alle Empfehlungen
//    @GetMapping
//    public ResponseEntity<List<RecommendationResponseDTO>> getAll() {
//        List<Recommendation> entities = recommendationRepository.findAll();
//        List<RecommendationResponseDTO> dtos = entities.stream()
//                .map(RecommendationMapper::toDTO)
//                .toList();
//
//        return ResponseEntity.ok(dtos);
//    }
//
//    // 📬 GET: Empfehlungen nach Benutzer
//    @GetMapping("/by-user/{userId}")
//    public ResponseEntity<List<RecommendationResponseDTO>> getByUser(@PathVariable Long userId) {
//        Optional<User> userOpt = userRepository.findById(userId);
//        if (userOpt.isEmpty()) {
//            return ResponseEntity.notFound().build();
//        }
//
//        List<Recommendation> recommendations = recommendationRepository.findByRecommendedBy(userOpt.get());
//
//        List<RecommendationResponseDTO> dtos = recommendations.stream()
//                .map(RecommendationMapper::toDTO)
//                .toList();
//
//        return ResponseEntity.ok(dtos);
//    }
//
//    // 📬 PUT: Empfehlung aktualisieren
//    @PutMapping("/{id}")
//    public ResponseEntity<Recommendation> updateRecommendation(@PathVariable Long id, @RequestBody Recommendation recommendationDetails) {
//        Optional<Recommendation> optionalRecommendation = recommendationRepository.findById(id);
//        if (optionalRecommendation.isPresent()) {
//            Recommendation recommendation = optionalRecommendation.get();
//            recommendation.setCandidateFirstname(recommendationDetails.getCandidateFirstname());
//            recommendation.setCandidateLastname(recommendationDetails.getCandidateLastname());
//            recommendation.setPosition(recommendationDetails.getPosition());
//            recommendation.setStatus(recommendationDetails.getStatus());
//
//            recommendationRepository.save(recommendation);
//            return ResponseEntity.ok(recommendation);
//        } else {
//            return ResponseEntity.notFound().build();
//        }
//    }
//
//    // 📬 DELETE: Empfehlung löschen
//    @DeleteMapping("/{id}")
//    public ResponseEntity<Void> deleteRecommendation(@PathVariable Long id) {
//        if (recommendationRepository.existsById(id)) {
//            recommendationRepository.deleteById(id);
//            return ResponseEntity.noContent().build();
//        } else {
//            return ResponseEntity.notFound().build();
//        }
//    }
//}
