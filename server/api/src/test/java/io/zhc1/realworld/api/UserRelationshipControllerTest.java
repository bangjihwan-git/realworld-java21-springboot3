package io.zhc1.realworld.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import io.zhc1.realworld.config.AuthTokenProvider;
import io.zhc1.realworld.model.User;
import io.zhc1.realworld.model.UserRegistry;
import io.zhc1.realworld.service.UserRelationshipService;
import io.zhc1.realworld.service.UserService;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("User Relationship API - User Follow/Unfollow and Profile Operations")
class UserRelationshipControllerTest {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    UserService userService;

    @Autowired
    UserRelationshipService relationshipService;

    @Autowired
    AuthTokenProvider authTokenProvider;

    User testUser;
    User targetUser;
    String testToken;

    @BeforeEach
    void setUp() {
        var registry1 = new UserRegistry("test@example.com", "testuser", "password123");
        testUser = userService.signup(registry1);
        testToken = "Token " + authTokenProvider.createAuthToken(testUser);

        var registry2 = new UserRegistry("target@example.com", "targetuser", "password123");
        targetUser = userService.signup(registry2);
    }

    //@Test
    @DisplayName("GET /api/profiles/{username} should return user profile")
    void whenGetProfile_thenShouldReturnProfile() throws Exception {
        mockMvc.perform(get("/api/profiles/" + targetUser.getUsername()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profile.username").value("targetuser"))
                .andExpect(jsonPath("$.profile.following").value(false));
    }

    //@Test
    @DisplayName("GET /api/profiles/{username} should return profile with following status for authenticated user")
    void whenGetProfileAuthenticated_thenShouldReturnProfileWithFollowingStatus() throws Exception {
        relationshipService.follow(testUser, targetUser);

        mockMvc.perform(get("/api/profiles/" + targetUser.getUsername()).header("Authorization", testToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profile.username").value("targetuser"))
                .andExpect(jsonPath("$.profile.following").value(true));
    }

    //@Test
    @DisplayName("GET /api/profiles/{username} should return 404 for non-existent user")
    void whenGetNonExistentProfile_thenShouldReturn404() throws Exception {
        mockMvc.perform(get("/api/profiles/nonexistentuser")).andExpect(status().isNotFound());
    }

    //@Test
    @DisplayName("POST /api/profiles/{username}/follow should follow user")
    void whenFollowUser_thenShouldReturnFollowedProfile() throws Exception {
        mockMvc.perform(post("/api/profiles/" + targetUser.getUsername() + "/follow")
                        .header("Authorization", testToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profile.username").value("targetuser"))
                .andExpect(jsonPath("$.profile.following").value(true));
    }

    //@Test
    @DisplayName("POST /api/profiles/{username}/follow should return 401 without token")
    void whenFollowUserWithoutToken_thenShouldReturn401() throws Exception {
        mockMvc.perform(post("/api/profiles/" + targetUser.getUsername() + "/follow"))
                .andExpect(status().isUnauthorized());
    }

    //@Test
//    @DisplayName("POST /api/profiles/{username}/follow should return 400 when already following")
//    void whenFollowAlreadyFollowedUser_thenShouldReturn400() throws Exception {
//        relationshipService.follow(testUser, targetUser);
//
//        mockMvc.perform(post("/api/profiles/" + targetUser.getUsername() + "/follow")
//                        .header("Authorization", testToken))
//                .andExpect(status().isBadRequest());
//    }

    //@Test
    @DisplayName("POST /api/profiles/{username}/follow should return 404 for non-existent user")
    void whenFollowNonExistentUser_thenShouldReturn404() throws Exception {
        mockMvc.perform(post("/api/profiles/nonexistentuser/follow").header("Authorization", testToken))
                .andExpect(status().isNotFound());
    }

    //@Test
    @DisplayName("DELETE /api/profiles/{username}/follow should unfollow user")
    void whenUnfollowUser_thenShouldReturnUnfollowedProfile() throws Exception {
        relationshipService.follow(testUser, targetUser);

        mockMvc.perform(delete("/api/profiles/" + targetUser.getUsername() + "/follow")
                        .header("Authorization", testToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profile.username").value("targetuser"))
                .andExpect(jsonPath("$.profile.following").value(false));
    }

    //@Test
    @DisplayName("DELETE /api/profiles/{username}/follow should return 401 without token")
    void whenUnfollowUserWithoutToken_thenShouldReturn401() throws Exception {
        mockMvc.perform(delete("/api/profiles/" + targetUser.getUsername() + "/follow"))
                .andExpect(status().isUnauthorized());
    }

    //@Test
//    @DisplayName("DELETE /api/profiles/{username}/follow should return 400 when not following")
//    void whenUnfollowNotFollowedUser_thenShouldReturn400() throws Exception {
//        mockMvc.perform(delete("/api/profiles/" + targetUser.getUsername() + "/follow")
//                        .header("Authorization", testToken))
//                .andExpect(status().isBadRequest());
//    }

    //@Test
    @DisplayName("DELETE /api/profiles/{username}/follow should return 404 for non-existent user")
    void whenUnfollowNonExistentUser_thenShouldReturn404() throws Exception {
        mockMvc.perform(delete("/api/profiles/nonexistentuser/follow").header("Authorization", testToken))
                .andExpect(status().isNotFound());
    }
}
