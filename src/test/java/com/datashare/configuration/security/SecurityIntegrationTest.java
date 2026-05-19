package com.datashare.configuration.security;

import com.datashare.dto.auth.LoginResponse;
import com.datashare.dto.auth.RegisterRequest;
import com.datashare.exception.NotFoundException;
import com.datashare.service.FileService;
import com.datashare.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private FileService fileService;

    @Test
    void shouldAllowPublicRegisterEndpoint() throws Exception {
        doNothing().when(userService).register(any(RegisterRequest.class));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "test@datashare.com",
                                  "password": "password1234"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("User registered successfully"));
    }

    @Test
    void shouldAllowPublicLoginEndpoint() throws Exception {
        when(userService.login(any())).thenReturn(new LoginResponse("fake-jwt-token"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "test@datashare.com",
                                  "password": "password1234"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("fake-jwt-token"));
    }

    @Test
    void shouldRejectFilesEndpointWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/files"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectFilesEndpointWithInvalidJwt() throws Exception {
        mockMvc.perform(get("/api/files")
                        .header("Authorization", "Bearer invalid.jwt.token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "test@datashare.com")
    void shouldAllowFilesEndpointWithAuthentication() throws Exception {
        when(fileService.getUserFiles(any())).thenReturn(List.of());

        mockMvc.perform(get("/api/files"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void shouldRejectDeleteEndpointWithoutAuthentication() throws Exception {
        mockMvc.perform(delete("/api/files/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectDeleteEndpointWithInvalidJwt() throws Exception {
        mockMvc.perform(delete("/api/files/1")
                        .header("Authorization", "Bearer invalid.jwt.token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "test@datashare.com")
    void shouldAllowDeleteEndpointWithAuthentication() throws Exception {
        mockMvc.perform(delete("/api/files/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldAllowPublicDownloadEndpointWithoutAuthentication() throws Exception {
        when(fileService.downloadByToken("missing-token"))
                .thenThrow(new NotFoundException("File not found"));

        mockMvc.perform(get("/download/missing-token"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("File not found"));
    }
}