package io.zhc1.realworld.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import io.zhc1.realworld.config.AuthTokenProvider;
import io.zhc1.realworld.model.User;
import io.zhc1.realworld.model.UserRegistry;
import io.zhc1.realworld.service.UserService;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@DisplayName("User API - User Registration, Authentication, and Profile Management")
class UserControllerTest {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    UserService userService;

    @Autowired
    AuthTokenProvider authTokenProvider;

    User testUser;
    String testToken;

    @BeforeEach
    void setUp() {
        var registry = new UserRegistry("test@example.com", "testuser", "password123");
        testUser = userService.signup(registry);
        testToken = "Token " + authTokenProvider.createAuthToken(testUser);
    }

    @Test
    @DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
    @DisplayName("POST /api/users should create new user and redirect to login")
    void whenSignup_thenShouldCreateUserAndRedirect() throws Exception {
        String signupJson =
                """
                {
                    "user": {
                        "email": "new@example.com",
                        "username": "newuser",
                        "password": "newpass123"
                    }
                }
                """;

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signupJson))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
    @DisplayName("POST /api/users should return 400 when user already exists")
    void whenSignupWithExistingUser_thenShouldReturn400() throws Exception {
        String signupJson =
                """
                {
                    "user": {
                        "email": "test@example.com",
                        "username": "testuser",
                        "password": "password123"
                    }
                }
                """;

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signupJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
    @DisplayName("POST /api/users/login should return user with token on valid credentials")
    void whenLoginWithValidCredentials_thenShouldReturnUserAndToken() throws Exception {
        String loginJson =
                """
                {
                    "user": {
                        "email": "test@example.com",
                        "password": "password123"
                    }
                }
                """;

        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.user.email").value("test@example.com"))
                .andExpect(jsonPath("$.user.username").value("testuser"))
                .andExpect(jsonPath("$.user.token").exists());
    }

    @Test
    @DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
    @DisplayName("POST /api/users/login should return 400 with invalid credentials")
    void whenLoginWithInvalidCredentials_thenShouldReturn400() throws Exception {
        String loginJson =
                """
                {
                    "user": {
                        "email": "test@example.com",
                        "password": "wrongpassword"
                    }
                }
                """;

        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
    @DisplayName("GET /api/user should return current user with valid token")
    void whenGetUserWithValidToken_thenShouldReturnUser() throws Exception {
        mockMvc.perform(get("/api/user").header("Authorization", testToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.email").value("test@example.com"))
                .andExpect(jsonPath("$.user.username").value("testuser"))
                .andExpect(jsonPath("$.user.token").exists());
    }

    @Test
    @DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
    @DisplayName("GET /api/user should return 401 without token")
    void whenGetUserWithoutToken_thenShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/user")).andExpect(status().isUnauthorized());
    }

    @Test
    @DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
    @DisplayName("PUT /api/user should update user details")
    void whenUpdateUser_thenShouldReturnUpdatedUser() throws Exception {
        String updateJson =
                """
                {
                    "user": {
                        "email": "updated@example.com",
                        "username": "updateduser",
                        "bio": "I am a test user",
                        "image": "https://example.com/avatar.jpg"
                    }
                }
                """;

        mockMvc.perform(put("/api/user")
                        .header("Authorization", testToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.email").value("updated@example.com"))
                .andExpect(jsonPath("$.user.username").value("updateduser"))
                .andExpect(jsonPath("$.user.bio").value("I am a test user"))
                .andExpect(jsonPath("$.user.image").value("https://example.com/avatar.jpg"));
    }

    @Test
    @DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
    @DisplayName("PUT /api/user should return 401 without token")
    void whenUpdateUserWithoutToken_thenShouldReturn401() throws Exception {
        String updateJson =
                """
                {
                    "user": {
                        "email": "updated@example.com"
                    }
                }
                """;

        mockMvc.perform(put("/api/user").contentType(MediaType.APPLICATION_JSON).content(updateJson))
                .andExpect(status().isUnauthorized());
    }
}
