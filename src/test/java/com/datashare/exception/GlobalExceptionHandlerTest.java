package com.datashare.exception;

import com.datashare.dto.common.ErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void shouldHandleBadRequestException() {
        ResponseEntity<ErrorResponse> response = handler.handleBadRequest(
                new BadRequestException("Bad request message")
        );

        assertEquals(400, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("Bad request message", response.getBody().message());
        assertEquals(400, response.getBody().status());
        assertNotNull(response.getBody().timestamp());
    }

    @Test
    void shouldHandleNotFoundException() {
        ResponseEntity<ErrorResponse> response = handler.handleNotFound(
                new NotFoundException("File not found")
        );

        assertEquals(404, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("File not found", response.getBody().message());
        assertEquals(404, response.getBody().status());
        assertNotNull(response.getBody().timestamp());
    }

    @Test
    void shouldHandleForbiddenException() {
        ResponseEntity<ErrorResponse> response = handler.handleForbidden(
                new ForbiddenException("Forbidden action")
        );

        assertEquals(403, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("Forbidden action", response.getBody().message());
        assertEquals(403, response.getBody().status());
        assertNotNull(response.getBody().timestamp());
    }

    @Test
    void shouldHandleValidationExceptionWithFieldMessage() {
        BeanPropertyBindingResult bindingResult =
                new BeanPropertyBindingResult(new Object(), "object");
        bindingResult.addError(new FieldError(
                "object",
                "email",
                "Email is required"
        ));

        MethodArgumentNotValidException exception =
                new MethodArgumentNotValidException(null, bindingResult);

        ResponseEntity<ErrorResponse> response = handler.handleValidation(exception);

        assertEquals(400, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("email: Email is required", response.getBody().message());
        assertEquals(400, response.getBody().status());
        assertNotNull(response.getBody().timestamp());
    }

    @Test
    void shouldHandleValidationExceptionWithDefaultMessage() {
        BeanPropertyBindingResult bindingResult =
                new BeanPropertyBindingResult(new Object(), "object");

        MethodArgumentNotValidException exception =
                new MethodArgumentNotValidException(null, bindingResult);

        ResponseEntity<ErrorResponse> response = handler.handleValidation(exception);

        assertEquals(400, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("Validation error", response.getBody().message());
        assertEquals(400, response.getBody().status());
        assertNotNull(response.getBody().timestamp());
    }

    @Test
    void shouldHandleGenericException() {
        ResponseEntity<ErrorResponse> response = handler.handleGeneric(
                new RuntimeException("Unexpected")
        );

        assertEquals(500, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("Internal server error", response.getBody().message());
        assertEquals(500, response.getBody().status());
        assertNotNull(response.getBody().timestamp());
    }

    @Test
    void shouldHandleBadCredentialsException() {
        ResponseEntity<ErrorResponse> response = handler.handleBadCredentials(
                new BadCredentialsException("Invalid credentials"));

        assertEquals(401, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("Invalid credentials", response.getBody().message());
        assertEquals(401, response.getBody().status());
        assertNotNull(response.getBody().timestamp());
    }
}