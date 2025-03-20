package com.empfehlo.empfehlungsapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // CSRF deaktivieren, z. B. für Postman-Tests
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/users/register-employee",
                                "/api/users/register-hr",
                                "/api/users/login"
                        ).permitAll()  // Erlaube diese Endpunkte für alle
                        .anyRequest().authenticated()
                ).httpBasic(); // oder formLogin() falls benötigt

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
