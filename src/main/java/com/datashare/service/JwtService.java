package com.datashare.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

// Service de génération et validation des tokens JWT.
@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration-ms}")
    private long expirationMs;

    private SecretKey secretKey;

    // Initialise la clé signée à partir du secret fourni dans la configuration.
    @PostConstruct
    void init() {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    // Génère un JWT simple contenant l'email de l'utilisateur comme sujet.
    public String generateToken(String email) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(email)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }

    // Extrait l'email contenu dans le token.
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    // Vérifie si le token est encore valide.
    public boolean isTokenValid(String token) {
        return !extractAllClaims(token).getExpiration().before(new Date());
    }

    // Extrait l'ensemble des claims du token.
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
