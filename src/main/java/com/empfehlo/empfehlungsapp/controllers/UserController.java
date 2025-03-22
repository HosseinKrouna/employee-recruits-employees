package com.empfehlo.empfehlungsapp.controllers;

import com.empfehlo.empfehlungsapp.models.LoginRequest;
import com.empfehlo.empfehlungsapp.models.User;
import com.empfehlo.empfehlungsapp.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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
        user.setRole("MITARBEITER"); // Immer "MITARBEITER"
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
        user.setRole("HR"); // Immer "HR"
        userRepository.save(user);
        return ResponseEntity.ok("✅ HR-Konto erstellt!");
    }

    /**
     * ✅ Login für alle Nutzer (Mitarbeiter & HR)
     */
    @PostMapping("/login")
    public ResponseEntity<String> loginUser(@RequestBody LoginRequest loginRequest) {
        Optional<User> userOptional = userRepository.findByUsername(loginRequest.getUsername());

        if (userOptional.isEmpty()) {
            return ResponseEntity.status(401).body("⚠ Benutzer nicht gefunden!");
        }

        User user = userOptional.get();

        // Hier das Klartext-Passwort mit dem gespeicherten (gehashten) Passwort vergleichen
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("⚠ Ungültige Anmeldedaten!");
        }

        return ResponseEntity.ok("✅ Login erfolgreich! Willkommen, " + user.getUsername());
    }

    // GET für alle Benutzer
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userRepository.findAll();
        return ResponseEntity.ok(users);
    }

    // GET für einen einzelnen Benutzer anhand der ID
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        Optional<User> userOpt = userRepository.findById(id);
        return userOpt.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }


}
