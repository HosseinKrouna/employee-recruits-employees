package com.empfehlo.empfehlungsapp.controllers;

import com.empfehlo.empfehlungsapp.dtos.LoginResponseDTO;
import com.empfehlo.empfehlungsapp.models.LoginRequest;
import com.empfehlo.empfehlungsapp.models.User;
import com.empfehlo.empfehlungsapp.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @PostMapping("/register-employee")
    public ResponseEntity<String> registerEmployee(@RequestBody User user) {
        return registerUser(user, "MITARBEITER");
    }

    @PostMapping("/register-hr")
    public ResponseEntity<String> registerHR(@RequestBody User user) {
        return registerUser(user, "HR");
    }

    private ResponseEntity<String> registerUser(User user, String role) {
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("⚠ Benutzer existiert bereits!");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(role);
        userRepository.save(user);

        String successMessage = role.equals("HR") ?
                "✅ HR-Konto erstellt!" :
                "✅ Mitarbeiter-Registrierung erfolgreich!";

        return ResponseEntity.ok(successMessage);
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest loginRequest) {
        Optional<User> userOptional = userRepository.findByUsername(loginRequest.getUsername());

        if (userOptional.isEmpty() || !isPasswordValid(userOptional.get(), loginRequest.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Ungültige Anmeldedaten!");
        }

        return ResponseEntity.ok(toLoginResponse(userOptional.get()));
    }

    private boolean isPasswordValid(User user, String rawPassword) {
        return passwordEncoder.matches(rawPassword, user.getPassword());
    }

    private LoginResponseDTO toLoginResponse(User user) {
        return new LoginResponseDTO(user.getId(), user.getUsername(), user.getRole());
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}