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
        // Hole die ID des eingeloggten Benutzers aus dem Security Context
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long loggedInUserId = getUserIdFromAuthentication(authentication);

        if (loggedInUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Benutzer nicht authentifiziert.");
        }
        // Optional: Prüfen, ob die übergebene UserID im DTO (falls vorhanden) mit dem eingeloggten User übereinstimmt
        if (dto.getUserId() != null && !dto.getUserId().equals(loggedInUserId)) {
            // Normalerweise sollte die UserId aus dem Token/Security Context genommen werden, nicht aus dem DTO.
            // Wenn sie im DTO ist, könnte man hier einen Fehler werfen oder die ID aus dem Context verwenden.
            // Hier gehen wir davon aus, dass die ID aus dem Context die maßgebliche ist.
            System.out.println("Warnung: UserId im DTO (" + dto.getUserId() + ") ignoriert, verwende eingeloggte UserID (" + loggedInUserId + ")");
        }
        // dto.setUserId(loggedInUserId); // Setze die korrekte UserId oder stelle sicher, dass der Mapper sie verwendet

        Optional<User> userOpt = userRepository.findById(loggedInUserId); // Finde den eingeloggten Benutzer
        if (userOpt.isEmpty()) {
            // Sollte nicht passieren, wenn der User authentifiziert ist
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Authentifizierter Benutzer nicht in DB gefunden.");
        }

        // -- Rest der Methode wie vorher, aber stelle sicher, dass userOpt.get() verwendet wird --
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
        // Diese Methode wird durch SecurityConfig bereits auf HR beschränkt
        List<RecommendationResponseDTO> dtos = recommendationRepository.findAll().stream()
                .map(RecommendationMapper::toDTO)
                .collect(Collectors.toList()); // Sicherer als .toList() für ältere Java-Versionen evtl.
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getRecommendationsByRequestingUser(@PathVariable Long userId) {

        // 1. Hole Authentifizierung des aktuell anfragenden Benutzers
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // 2. Prüfe die Rolle des anfragenden Benutzers
        boolean isHr = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_HR"::equals);

        // 3. Hole die ID des anfragenden Benutzers
        Long loggedInUserId = getUserIdFromAuthentication(authentication);
        if (loggedInUserId == null) {
            // Konnte ID nicht ermitteln
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Benutzeridentifikation fehlgeschlagen.");
        }

        // 4. Autorisierungslogik ANWENDEN
        if (!isHr && !loggedInUserId.equals(userId)) {
            // Mitarbeiter versucht, Daten eines anderen Benutzers abzurufen
            System.out.println("Zugriffsversuch von Mitarbeiter " + loggedInUserId + " auf Daten von User " + userId + " blockiert.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Zugriff verweigert."); // 403 Forbidden
        }

        // 5. Zugriff erlaubt (HR oder eigener Zugriff): Finde den Benutzer, für den die Empfehlungen abgerufen werden sollen
        Optional<User> targetUserOpt = userRepository.findById(userId);
        if (targetUserOpt.isEmpty()) {
            // Der Benutzer, dessen Empfehlungen gesucht werden, existiert nicht
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Benutzer mit ID " + userId + " nicht gefunden.");
        }

        // 6. Finde die Empfehlungen dieses Benutzers und gib sie zurück
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

        // Ermittle den Benutzernamen aus dem Principal
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails) {
            username = ((UserDetails) principal).getUsername();
        } else if (principal instanceof String) {
            username = (String) principal;
        } else {
            // Fallback oder Fehler, falls Principal ein unerwarteter Typ ist
            System.err.println("Unerwarteter Principal-Typ beim Extrahieren des Usernamens: " + principal.getClass().getName());
            // Optional: Versuche authentication.getName(), falls verfügbar
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

        // Finde den Benutzer im Repository anhand des Benutzernamens
        Optional<User> userOpt = userRepository.findByUsername(username);

        if (userOpt.isEmpty()) {
            // Dies sollte eigentlich nicht passieren, wenn der Benutzer authentifiziert ist,
            // da loadUserByUsername ihn vorher gefunden haben muss. Deutet auf Inkonsistenz hin.
            System.err.println("WARNUNG: Authentifizierter Benutzer '" + username + "' nicht im Repository gefunden!");
            return null;
        }

        // Gib die ID des gefundenen Benutzers zurück
        return userOpt.get().getId();
    }



    @PutMapping("/{id}")
    public ResponseEntity<?> updateRecommendation(@PathVariable Long id, @RequestBody Recommendation recommendationDetails) {
        // TODO: Berechtigungsprüfung hinzufügen!
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
        // TODO: Berechtigungsprüfung hinzufügen! (z.B. nur HR)
        if (recommendationRepository.existsById(id)) {
            recommendationRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}