package com.empfehlo.empfehlungsapp.services;

import com.empfehlo.empfehlungsapp.models.User;
import com.empfehlo.empfehlungsapp.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Benutzer nicht gefunden: " + username));

        // Erstelle UserDetails Objekt (Rolle als Authority hinzufügen)
        // Das Präfix "ROLE_" ist Konvention für Spring Security Rollen
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(), // Das gehashte Passwort
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole())) // Rolle hinzufügen
        );
    }
}