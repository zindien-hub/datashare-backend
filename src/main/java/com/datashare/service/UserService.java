package com.datashare.service;

import com.datashare.dto.auth.LoginRequest;
import com.datashare.dto.auth.LoginResponse;
import com.datashare.dto.auth.RegisterRequest;
import com.datashare.entities.User;
import com.datashare.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // Crée un nouvel utilisateur si l'email n'existe pas déjà.
    public void register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email already exists");
        }

        User user = new User();
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));

        userRepository.save(user);
    }

    // Vérifie les identifiants puis renvoie une réponse d'authentification.
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        boolean passwordMatches = passwordEncoder.matches(request.password(), user.getPasswordHash());

        if (!passwordMatches) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        // todo: Remplacer ce token temporaire par un vrai JWT.
        return new LoginResponse("temporary-token");
    }
}
