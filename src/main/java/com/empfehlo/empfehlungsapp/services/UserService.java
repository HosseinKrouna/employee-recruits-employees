package com.empfehlo.empfehlungsapp.services;

import com.empfehlo.empfehlungsapp.models.User;
import com.empfehlo.empfehlungsapp.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public boolean userExists(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    public User registerUser(String username, String password, String role) {
        if (userExists(username)) {
            return null; // Benutzer existiert bereits
        }
        String hashedPassword = passwordEncoder.encode(password);
        User newUser = new User(username, hashedPassword, role);
        return userRepository.save(newUser);
    }

    public boolean validateUser(String username, String password) {
        Optional<User> user = userRepository.findByUsername(username);
        return user.map(value -> passwordEncoder.matches(password, value.getPassword())).orElse(false);
    }
}
