package com.datashare.configuration.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

// Configuration minimale des beans de sécurité utiles à l'authentification.
@Configuration
public class SecurityConfig {

    // Encodeur utilisé pour stocker les mots de passe hashés.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}