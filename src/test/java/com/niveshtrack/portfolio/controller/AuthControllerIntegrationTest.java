package com.niveshtrack.portfolio.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.niveshtrack.portfolio.dto.request.LoginRequest;
import com.niveshtrack.portfolio.dto.request.RegisterRequest;
import com.niveshtrack.portfolio.repository.RefreshTokenRepository;
import com.niveshtrack.portfolio.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for {@link AuthController}.
 *
 * <p>Uses an in-memory H2 database (test profile) for isolation.
 * Each test method runs in a transaction that is rolled back afterward.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("AuthController Integration Tests")
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    private static final String REGISTER_URL = "/api/auth/register";
    private static final String LOGIN_URL     = "/api/auth/login";
    private static final String REFRESH_URL   = "/api/auth/refresh";

    // ===== REGISTER =====

    @Nested
    @DisplayName("POST /api/auth/register")
    class Register {

        @Test
        @DisplayName("should register a new user and return 201 with JWT tokens")
        void shouldRegisterNewUser() throws Exception {
            RegisterRequest req = new RegisterRequest();
            req.setName("Priya Singh");
            req.setEmail("priya@example.com");
            req.setPassword("securePass123");

            mockMvc.perform(post(REGISTER_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.accessToken").isNotEmpty())
                    .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                    .andExpect(jsonPath("$.tokenType").value("Bearer"))
                    .andExpect(jsonPath("$.email").value("priya@example.com"))
                    .andExpect(jsonPath("$.name").value("Priya Singh"));
        }

        @Test
        @DisplayName("should return 409 when email already registered")
        void shouldReturn409WhenEmailAlreadyExists() throws Exception {
            // First registration
            RegisterRequest req = new RegisterRequest();
            req.setName("Duplicate User");
            req.setEmail("duplicate@example.com");
            req.setPassword("password123");

            mockMvc.perform(post(REGISTER_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isCreated());

            // Second registration with same email
            mockMvc.perform(post(REGISTER_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("should return 400 for missing required fields")
        void shouldReturn400ForMissingFields() throws Exception {
            RegisterRequest req = new RegisterRequest();
            req.setEmail("noname@example.com");
            // name and password missing

            mockMvc.perform(post(REGISTER_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("should return 400 for invalid email format")
        void shouldReturn400ForInvalidEmail() throws Exception {
            RegisterRequest req = new RegisterRequest();
            req.setName("Test User");
            req.setEmail("not-an-email");
            req.setPassword("password123");

            mockMvc.perform(post(REGISTER_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("should return 400 when password is too short")
        void shouldReturn400ForShortPassword() throws Exception {
            RegisterRequest req = new RegisterRequest();
            req.setName("Test User");
            req.setEmail("shortpass@example.com");
            req.setPassword("abc");   // too short

            mockMvc.perform(post(REGISTER_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest());
        }
    }

    // ===== LOGIN =====

    @Nested
    @DisplayName("POST /api/auth/login")
    class Login {

        private void createUser(String email, String password) throws Exception {
            RegisterRequest req = new RegisterRequest();
            req.setName("Login Test User");
            req.setEmail(email);
            req.setPassword(password);
            mockMvc.perform(post(REGISTER_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("should return 200 with tokens for valid credentials")
        void shouldLoginWithValidCredentials() throws Exception {
            createUser("login@example.com", "password123");

            LoginRequest loginReq = new LoginRequest();
            loginReq.setEmail("login@example.com");
            loginReq.setPassword("password123");

            mockMvc.perform(post(LOGIN_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginReq)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").isNotEmpty())
                    .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                    .andExpect(jsonPath("$.email").value("login@example.com"));
        }

        @Test
        @DisplayName("should return 401 for wrong password")
        void shouldReturn401ForWrongPassword() throws Exception {
            createUser("wrongpass@example.com", "correctPassword");

            LoginRequest loginReq = new LoginRequest();
            loginReq.setEmail("wrongpass@example.com");
            loginReq.setPassword("wrongPassword");

            mockMvc.perform(post(LOGIN_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginReq)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("should return 401 for non-existent user")
        void shouldReturn401ForNonExistentUser() throws Exception {
            LoginRequest loginReq = new LoginRequest();
            loginReq.setEmail("ghost@example.com");
            loginReq.setPassword("password123");

            mockMvc.perform(post(LOGIN_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginReq)))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ===== REFRESH TOKEN =====

    @Nested
    @DisplayName("POST /api/auth/refresh")
    class RefreshToken {

        @Test
        @DisplayName("should return new access token for valid refresh token")
        void shouldReturnNewAccessTokenForValidRefreshToken() throws Exception {
            // Register and get initial tokens
            RegisterRequest reg = new RegisterRequest();
            reg.setName("Refresh User");
            reg.setEmail("refresh@example.com");
            reg.setPassword("password123");

            MvcResult result = mockMvc.perform(post(REGISTER_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(reg)))
                    .andExpect(status().isCreated())
                    .andReturn();

            String body = result.getResponse().getContentAsString();
            String refreshToken = objectMapper.readTree(body).get("refreshToken").asText();

            // Use refresh token to get new access token
            mockMvc.perform(post(REFRESH_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"refreshToken\": \"" + refreshToken + "\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").isNotEmpty());
        }

        @Test
        @DisplayName("should return 401 for invalid/expired refresh token")
        void shouldReturn401ForInvalidRefreshToken() throws Exception {
            mockMvc.perform(post(REFRESH_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"refreshToken\": \"invalid-token-xyz\"}"))
                    .andExpect(status().isUnauthorized());
        }
    }
}
