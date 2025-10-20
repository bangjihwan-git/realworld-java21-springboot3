package io.zhc1.realworld.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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
import io.zhc1.realworld.model.Article;
import io.zhc1.realworld.model.User;
import io.zhc1.realworld.model.UserRegistry;
import io.zhc1.realworld.service.ArticleService;
import io.zhc1.realworld.service.UserService;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@DisplayName("Article Favorite API - Article Like/Unlike Operations")
class ArticleFavoriteControllerTest {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    UserService userService;

    @Autowired
    ArticleService articleService;

    @Autowired
    AuthTokenProvider authTokenProvider;

    User testUser;
    String testToken;
    Article testArticle;

    @BeforeEach
    void setUp() {
        var registry = new UserRegistry("test@example.com", "testuser", "password123");
        testUser = userService.signup(registry);
        testToken = "Token " + authTokenProvider.createAuthToken(testUser);

        var article = new Article(testUser, "Test Article", "Test Description", "Test Body");
        testArticle = articleService.write(article, null);
    }

    @Test
    @DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
    @DisplayName("POST /api/articles/{slug}/favorite should favorite article")
    void whenFavoriteArticle_thenShouldReturnFavoritedArticle() throws Exception {
        mockMvc.perform(post("/api/articles/" + testArticle.getSlug() + "/favorite")
                        .header("Authorization", testToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.article.favorited").value(true))
                .andExpect(jsonPath("$.article.favoritesCount").value(1));
    }

    @Test
    @DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
    @DisplayName("POST /api/articles/{slug}/favorite should return 401 without token")
    void whenFavoriteArticleWithoutToken_thenShouldReturn401() throws Exception {
        mockMvc.perform(post("/api/articles/" + testArticle.getSlug() + "/favorite"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
    @DisplayName("POST /api/articles/{slug}/favorite should return 400 when already favorited")
    void whenFavoriteAlreadyFavoritedArticle_thenShouldReturn400() throws Exception {
        // First favorite
        articleService.favorite(testUser, testArticle);

        // Try to favorite again
        mockMvc.perform(post("/api/articles/" + testArticle.getSlug() + "/favorite")
                        .header("Authorization", testToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
    @DisplayName("DELETE /api/articles/{slug}/favorite should unfavorite article")
    void whenUnfavoriteArticle_thenShouldReturnUnfavoritedArticle() throws Exception {
        // First favorite the article
        articleService.favorite(testUser, testArticle);

        mockMvc.perform(delete("/api/articles/" + testArticle.getSlug() + "/favorite")
                        .header("Authorization", testToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.article.favorited").value(false))
                .andExpect(jsonPath("$.article.favoritesCount").value(0));
    }

    @Test
    @DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
    @DisplayName("DELETE /api/articles/{slug}/favorite should return 401 without token")
    void whenUnfavoriteArticleWithoutToken_thenShouldReturn401() throws Exception {
        mockMvc.perform(delete("/api/articles/" + testArticle.getSlug() + "/favorite"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
    @DisplayName("DELETE /api/articles/{slug}/favorite should return 400 when not favorited")
    void whenUnfavoriteNotFavoritedArticle_thenShouldReturn400() throws Exception {
        mockMvc.perform(delete("/api/articles/" + testArticle.getSlug() + "/favorite")
                        .header("Authorization", testToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
    @DisplayName("POST /api/articles/{slug}/favorite should return 404 for non-existent article")
    void whenFavoriteNonExistentArticle_thenShouldReturn404() throws Exception {
        mockMvc.perform(post("/api/articles/non-existent-slug/favorite").header("Authorization", testToken))
                .andExpect(status().isNotFound());
    }
}
