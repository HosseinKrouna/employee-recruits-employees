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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.context.Context;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    private final PdfGeneratorService pdfGeneratorService;
    private final RecommendationRepository recommendationRepository;
    private final UserRepository userRepository;


    @Autowired
    public RecommendationController(PdfGeneratorService pdfGeneratorService,
                                    RecommendationRepository recommendationRepository,
                                    UserRepository userRepository) {
        this.pdfGeneratorService = pdfGeneratorService;
        this.recommendationRepository = recommendationRepository;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<?> createRecommendation(@RequestBody RecommendationRequestDTO dto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long loggedInUserId = getUserIdFromAuthentication(authentication);

        if (loggedInUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Benutzer nicht authentifiziert.");
        }
        if (dto.getUserId() != null && !dto.getUserId().equals(loggedInUserId)) {
            System.out.println("Warnung: UserId im DTO (" + dto.getUserId() + ") ignoriert, verwende eingeloggte UserID (" + loggedInUserId + ")");
        }

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Authentifizierter Benutzer nicht in DB gefunden.");
        }

        Recommendation recommendation = RecommendationMapper.toEntity(dto, userOpt.get());
        Recommendation saved = recommendationRepository.save(recommendation);

        try {
            String pdfFilename = pdfGeneratorService.generateAndStorePdf(dto);
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
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getRecommendationsByRequestingUser(@PathVariable Long userId) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        boolean isHr = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_HR"::equals);

        Long loggedInUserId = getUserIdFromAuthentication(authentication);
        if (loggedInUserId == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Benutzeridentifikation fehlgeschlagen.");
        }

        if (!isHr && !loggedInUserId.equals(userId)) {
            System.out.println("Zugriffsversuch von Mitarbeiter " + loggedInUserId + " auf Daten von User " + userId + " blockiert.");
        }

        Optional<User> targetUserOpt = userRepository.findById(userId);
        if (targetUserOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Benutzer mit ID " + userId + " nicht gefunden.");
        }

        List<RecommendationResponseDTO> dtos = recommendationRepository.findByRecommendedBy(targetUserOpt.get()).stream()
                .map(RecommendationMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    /**
     * Extrahiert die numerische Benutzer-ID des aktuell authentifizierten Benutzers.
     * Liest den Benutzernamen aus dem Authentication-Objekt und sucht den
     * Benutzer im Repository, um dessen ID zu erhalten.
     *
     * @param authentication Das Authentication-Objekt aus dem SecurityContext.
     * @return Die Long ID des Benutzers oder null, wenn nicht authentifiziert oder Benutzer nicht gefunden wurde.
     */
    private Long getUserIdFromAuthentication(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal() == null) {
            System.err.println("Versuch, UserID von nicht authentifiziertem oder null Principal zu holen.");
            return null;
        }

        String username;

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails) {
            username = ((UserDetails) principal).getUsername();
        } else if (principal instanceof String) {
            username = (String) principal;
        } else {
            System.err.println("Unerwarteter Principal-Typ beim Extrahieren des Usernamens: " + principal.getClass().getName());
            username = authentication.getName();
            if (username == null || username.equalsIgnoreCase("anonymousUser")){
                System.err.println("Principal ist anonymousUser oder Name ist null.");
                return null;
            }

        }

        if (username == null) {
            System.err.println("Konnte keinen Benutzernamen aus der Authentifizierung extrahieren.");
            return null;
        }

        Optional<User> userOpt = userRepository.findByUsername(username);

        if (userOpt.isEmpty()) {
            System.err.println("WARNUNG: Authentifizierter Benutzer '" + username + "' nicht im Repository gefunden!");
            return null;
        }

        return userOpt.get().getId();
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