package com.empfehlo.empfehlungsapp.config;

import com.empfehlo.empfehlungsapp.security.JwtRequestFilter;
import com.empfehlo.empfehlungsapp.services.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod; // <-- Wichtig: HttpMethod importieren
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private JwtRequestFilter jwtRequestFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // CSRF deaktivieren

                // Autorisierungsregeln definieren (WICHTIG: Die Reihenfolge ist entscheidend!)
                .authorizeHttpRequests(auth -> auth
                        // 1. Öffentliche Endpunkte (Login, Registrierung)
                        .requestMatchers("/api/users/login", "/api/users/register-employee", "/api/users/register-hr").permitAll()

                        // 2. Empfehlungs-Endpunkte (Recommendations)
                        // Nur MITARBEITER dürfen neue Empfehlungen erstellen (POST)
                        .requestMatchers(HttpMethod.POST, "/api/recommendations").hasRole("MITARBEITER")
                        // HR darf ALLE Empfehlungen abrufen (GET auf /api/recommendations)
                        .requestMatchers(HttpMethod.GET, "/api/recommendations").hasRole("HR")
                        // HR und MITARBEITER dürfen Empfehlungen für einen spezifischen Nutzer abrufen
                        // (GET auf /api/recommendations/user/{userId}).
                        // Die Logik, dass ein MITARBEITER nur seine eigenen sehen darf,
                        // muss im Controller/Service implementiert werden!
                        .requestMatchers(HttpMethod.GET, "/api/recommendations/user/**").hasAnyRole("HR", "MITARBEITER")
                        // Beispiel: Angenommen, es gäbe einen GET-Endpunkt für eine einzelne Empfehlung
                        // .requestMatchers(HttpMethod.GET, "/api/recommendations/{id}").hasAnyRole("HR", "MITARBEITER") // Controller/Service prüft Ownership für MITARBEITER
                        // Beispiel: Angenommen, nur HR darf Empfehlungen löschen oder ändern
                        // .requestMatchers(HttpMethod.DELETE, "/api/recommendations/**").hasRole("HR")
                        // .requestMatchers(HttpMethod.PUT, "/api/recommendations/**").hasRole("HR")


                        // 3. Datei-Endpunkte (Files)
                        // HR darf auf alle Dateioperationen zugreifen (download, download-generated)
                        .requestMatchers("/api/files/**").hasAnyRole("HR", "MITARBEITER")

                        .requestMatchers(HttpMethod.PUT, "/api/recommendations/*/status").hasRole("HR")
                        // .requestMatchers(HttpMethod.PATCH, "/api/recommendations/*/status").hasRole("HR") //
                        // 4. Fallback: Alle anderen Anfragen erfordern Authentifizierung
                        .anyRequest().authenticated()
                )

                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // Stateless Session
                )
                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class); // JWT Filter

        return http.build();
    }
}