package com.empfehlo.empfehlungsapp.security; // Beispielpaket

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct; // Korrekter Import für Spring Boot 3
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secretString;

    @Value("${jwt.expiration}")
    private long expirationTime;

    private SecretKey secretKey; // Verwende SecretKey

    @PostConstruct
    protected void init() {
        byte[] keyBytes = Base64.getDecoder().decode(this.secretString.getBytes(StandardCharsets.UTF_8));
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    // Token generieren (unverändert)
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, userDetails.getUsername());
    }

    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(secretKey, SignatureAlgorithm.HS256) // <- Beachte: SignatureAlgorithm ist hier noch ok
                .compact();
    }

    // Token validieren (unverändert)
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username != null && username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    // Hilfsmethoden zum Extrahieren (unverändert in der Logik)
    public String extractUsername(String token) {
        try {
            return extractClaim(token, Claims::getSubject);
        } catch (Exception e) {
            System.err.println("Fehler beim Extrahieren des Benutzernamens (wahrscheinlich ungültiges Token): " + e.getMessage());
            return null;
        }
    }

    public Date extractExpiration(String token) {
        try {
            return extractClaim(token, Claims::getExpiration);
        } catch (Exception e) {
            System.err.println("Fehler beim Extrahieren des Ablaufdatums: " + e.getMessage());
            return null;
        }
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        if (claims != null) {
            return claimsResolver.apply(claims);
        }
        return null;
    }

    // --- ANGEPASSTE Methode extractAllClaims für jjwt 0.12.x ---
    private Claims extractAllClaims(String token) {
        try {
            // Verwende die neue empfohlene API für 0.12.x
            return Jwts.parser()                  // Start mit parser()
                    .verifyWith(secretKey)        // Den Schlüssel zum Verifizieren übergeben
                    .build()                      // Den Parser bauen
                    .parseSignedClaims(token)     // Parsen und Signatur prüfen (ersetzt parseClaimsJws)
                    .getPayload();                // Die Claims (Payload) holen
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            System.err.println("JWT ist abgelaufen: " + e.getMessage());
            return null;
        } catch (io.jsonwebtoken.JwtException e) { // Fängt andere JWT-Fehler
            System.err.println("Fehler beim Parsen/Validieren des JWT: " + e.getMessage());
            return null;
        } catch (IllegalArgumentException e) { // Falls Token-String null oder leer ist
            System.err.println("Ungültiges Token-Argument: " + e.getMessage());
            return null;
        }
    }

    // isTokenExpired (unverändert)
    private Boolean isTokenExpired(String token) {
        Date expiration = extractExpiration(token);
        return expiration == null || expiration.before(new Date());
    }
}