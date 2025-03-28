package com.empfehlo.empfehlungsapp.controllers;

import com.empfehlo.empfehlungsapp.repositories.RecommendationRepository;
import com.empfehlo.empfehlungsapp.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dev")
public class DevToolsController {

    @Autowired
    private RecommendationRepository recommendationRepository;
    @Autowired
    private UserRepository userRepository;

    @DeleteMapping("/reset")
    public ResponseEntity<String> resetAll() {
        recommendationRepository.deleteAll();
        userRepository.deleteAll();
        return ResponseEntity.ok("✅ Alle Testdaten gelöscht.");
    }

    // Nur Benutzer löschen
    @DeleteMapping("/reset-users")
    public ResponseEntity<String> resetUsersOnly() {
        recommendationRepository.deleteAll(); // zuerst abhängige Empfehlungen löschen
        userRepository.deleteAll();           // dann Benutzer
        return ResponseEntity.ok("✅ Benutzer & zugehörige Empfehlungen gelöscht.");
    }

}


