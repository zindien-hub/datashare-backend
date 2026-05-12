package com.datashare.controller;

import com.datashare.dto.auth.LoginRequest;
import com.datashare.dto.auth.LoginResponse;
import com.datashare.dto.auth.RegisterRequest;
import com.datashare.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints d'authentification")
public class AuthController {

    private final UserService userService;

    // Endpoint d'inscription d'un nouvel utilisateur.
    @Operation(summary = "Register a new user", description = "Crée un nouvel utilisateur")
    @ApiResponse(responseCode = "201", description = "Utilisateur créé avec succès")
    @ApiResponse(responseCode = "400", description = "Requête invalide")
    @PostMapping("/register")
    public ResponseEntity<MessageResponse> register(@Valid @RequestBody RegisterRequest request) {
        userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new MessageResponse("User registered successfully"));
    }

    // Endpoint de connexion utilisateur.
    @Operation(summary = "Authenticate a user", description = "Authentifie un utilisateur et retourne un JWT")
    @ApiResponse(responseCode = "200", description = "Authentification réussie")
    @ApiResponse(responseCode = "401", description = "Identifiants invalides")
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = userService.login(request);
        return ResponseEntity.ok(response);
    }

    // Réponse simple utilisée pour les messages de succès.
    public record MessageResponse(String message) {
    }
}
