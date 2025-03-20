package com.empfehlo.empfehlungsapp.controllers;

import com.empfehlo.empfehlungsapp.models.User;
import com.empfehlo.empfehlungsapp.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * ✅ Mitarbeiter-Registrierung (Kann nur über JavaFX genutzt werden)
     */
    @PostMapping("/register-employee")
    public ResponseEntity<String> registerEmployee(@RequestBody User user) {
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("⚠ Benutzer existiert bereits!");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        return ResponseEntity.ok("✅ Mitarbeiter-Registrierung erfolgreich!");
    }

    /**
     * ✅ HR-Registrierung (Kann nur über Postman genutzt werden)
     */
    @PostMapping("/register-hr")
    public ResponseEntity<String> registerHR(@RequestBody User user) {
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("⚠ Benutzer existiert bereits!");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        return ResponseEntity.ok("✅ HR-Konto erstellt!");
    }

    /**
     * ✅ Login für alle Nutzer (Mitarbeiter & HR)
     */
    @PostMapping("/login")
    public ResponseEntity<String> loginUser(@RequestParam String username, @RequestParam String password) {
        Optional<User> userOptional = userRepository.findByUsername(username);

        if (userOptional.isEmpty()) {
            return ResponseEntity.status(401).body("⚠ Benutzer nicht gefunden!");
        }

        User user = userOptional.get();

        if (!passwordEncoder.matches(password, user.getPassword())) {
            return ResponseEntity.status(401).body("⚠ Ungültige Anmeldedaten!");
        }

        return ResponseEntity.ok("✅ Login erfolgreich! Willkommen, " + user.getUsername());
    }

}
