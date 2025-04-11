package com.empfehlo.empfehlungsapp.config;

import com.empfehlo.empfehlungsapp.security.JwtRequestFilter;
import com.empfehlo.empfehlungsapp.services.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/users/login", "/api/users/register-employee", "/api/users/register-hr").permitAll()

                        .requestMatchers(HttpMethod.POST, "/api/recommendations").hasRole("MITARBEITER")
                        .requestMatchers(HttpMethod.GET, "/api/recommendations").hasRole("HR")
                        .requestMatchers(HttpMethod.GET, "/api/recommendations/user/**").hasAnyRole("HR", "MITARBEITER")


                        .requestMatchers("/api/files/**").hasRole("HR")

                        .anyRequest().authenticated()
                )

                .sessionManagement(session -> session
                )

        return http.build();
    }
}