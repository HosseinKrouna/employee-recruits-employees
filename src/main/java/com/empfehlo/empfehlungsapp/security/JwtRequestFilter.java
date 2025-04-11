package com.empfehlo.empfehlungsapp.security;

import com.empfehlo.empfehlungsapp.services.CustomUserDetailsService; // Dein UserDetailsService
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        final String authorizationHeader = request.getHeader("Authorization");

        String username = null;
        String jwt = null;

        // Token extrahieren (muss mit "Bearer " beginnen)
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            try {
                username = jwtUtil.extractUsername(jwt);
            } catch (Exception e) {
                // Token ungültig oder abgelaufen - Fehler loggen oder ignorieren
                System.err.println("Fehler beim Extrahieren des Benutzernamens aus JWT: " + e.getMessage());
            }
        }

        // Wenn Benutzername extrahiert wurde und noch keine Authentifizierung im Context ist
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

            // Wenn Token gültig ist, Authentifizierung im SecurityContext setzen
            if (jwtUtil.validateToken(jwt, userDetails)) {
                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                usernamePasswordAuthenticationToken
                        .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
                System.out.println("JWT validiert für Benutzer: " + username); // Logging
            } else {
                System.out.println("JWT ungültig für Benutzer: " + username); // Logging
            }
        } else if (username == null && jwt != null) {
            System.out.println("JWT vorhanden, aber Benutzername konnte nicht extrahiert werden."); // Logging
        }
        chain.doFilter(request, response); // Anfrage an den nächsten Filter weiterleiten
    }
}