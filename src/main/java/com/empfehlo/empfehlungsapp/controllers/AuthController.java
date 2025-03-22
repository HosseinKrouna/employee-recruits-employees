package com.empfehlo.empfehlungsapp.controllers;

import com.empfehlo.empfehlungsapp.models.User;
import com.empfehlo.empfehlungsapp.repositories.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthController(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody User newUser) {
        Optional<User> existingUser = userRepository.findByUsername(newUser.getUsername());
        if (existingUser.isPresent()) {
            return ResponseEntity.badRequest().body("Benutzername bereits vergeben.");
        }

        if (!"MITARBEITER".equalsIgnoreCase(newUser.getRole())) {
            return ResponseEntity.badRequest().body("Nur Mitarbeiter können sich registrieren.");
        }

        newUser.setPassword(passwordEncoder.encode(newUser.getPassword()));
        userRepository.save(newUser);

        return ResponseEntity.ok("Registrierung erfolgreich!");
    }
}

