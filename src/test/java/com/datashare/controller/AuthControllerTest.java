package com.datashare.controller;

import com.datashare.dto.auth.LoginRequest;
import com.datashare.dto.auth.LoginResponse;
import com.datashare.dto.auth.RegisterRequest;
import com.datashare.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthControllerTest {

    private final UserService userService = mock(UserService.class);
    private final AuthController authController = new AuthController(userService);

    @Test
    void shouldRegisterUser() {
        RegisterRequest request = new RegisterRequest("test@datashare.com", "password1234");

        doNothing().when(userService).register(request);

        ResponseEntity<AuthController.MessageResponse> response = authController.register(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("User registered successfully", response.getBody().message());

        verify(userService).register(request);
    }

    @Test
    void shouldLoginUser() {
        LoginRequest request = new LoginRequest("test@datashare.com", "password1234");
        LoginResponse loginResponse = new LoginResponse("fake-jwt-token");

        when(userService.login(request)).thenReturn(loginResponse);

        ResponseEntity<LoginResponse> response = authController.login(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("fake-jwt-token", response.getBody().token());

        verify(userService).login(request);
    }
}