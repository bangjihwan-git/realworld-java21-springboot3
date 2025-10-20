package io.zhc1.realworld.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Set;

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
import io.zhc1.realworld.model.Article;
import io.zhc1.realworld.model.Tag;
import io.zhc1.realworld.model.User;
import io.zhc1.realworld.model.UserRegistry;
import io.zhc1.realworld.service.ArticleService;
import io.zhc1.realworld.service.UserService;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@DisplayName("Article API - Article CRUD and Query Operations")
class ArticleControllerTest {

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
        testArticle = articleService.write(article, Set.of(new Tag("test"), new Tag("java")));
    }

    @Test
    @DisplayName("POST /api/articles should create new article")
    void whenPostArticle_thenShouldCreateArticle() throws Exception {
        String articleJson =
                """
                {
                    "article": {
                        "title": "New Article",
                        "description": "New Description",
                        "body": "New Body",
                        "tagList": ["test", "new"]
                    }
                }
                """;

        mockMvc.perform(post("/api/articles")
                        .header("Authorization", testToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(articleJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.article.title").value("New Article"))
                .andExpect(jsonPath("$.article.description").value("New Description"))
                .andExpect(jsonPath("$.article.body").value("New Body"))
                .andExpect(jsonPath("$.article.slug").exists());
    }

    @Test
    @DisplayName("POST /api/articles should return 401 without token")
    void whenPostArticleWithoutToken_thenShouldReturn401() throws Exception {
        String articleJson =
                """
                {
                    "article": {
                        "title": "New Article",
                        "description": "New Description",
                        "body": "New Body"
                    }
                }
                """;

        mockMvc.perform(post("/api/articles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(articleJson))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/articles should return list of articles")
    void whenGetArticles_thenShouldReturnArticles() throws Exception {
        mockMvc.perform(get("/api/articles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.articles").isArray())
                .andExpect(jsonPath("$.articlesCount").exists());
    }

    @Test
    @DisplayName("GET /api/articles with tag filter should return filtered articles")
    void whenGetArticlesWithTagFilter_thenShouldReturnFilteredArticles() throws Exception {
        mockMvc.perform(get("/api/articles").param("tag", "test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.articles").isArray());
    }

    @Test
    @DisplayName("GET /api/articles with pagination should respect limit and offset")
    void whenGetArticlesWithPagination_thenShouldRespectLimitAndOffset() throws Exception {
        mockMvc.perform(get("/api/articles").param("limit", "5").param("offset", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.articles").isArray());
    }

    @Test
    @DisplayName("GET /api/articles/{slug} should return article by slug")
    void whenGetArticleBySlug_thenShouldReturnArticle() throws Exception {
        mockMvc.perform(get("/api/articles/" + testArticle.getSlug()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.article.slug").value(testArticle.getSlug()))
                .andExpect(jsonPath("$.article.description").value("Test Description"))
                .andExpect(jsonPath("$.article.body").value("Test Body"));
    }

    @Test
    @DisplayName("GET /api/articles/{slug} should return 404 for non-existent article")
    void whenGetNonExistentArticle_thenShouldReturn404() throws Exception {
        mockMvc.perform(get("/api/articles/non-existent-slug")).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/articles/{slug} should update article")
    void whenUpdateArticle_thenShouldReturnUpdatedArticle() throws Exception {
        String updateJson =
                """
                {
                    "article": {
                        "title": "Updated Title",
                        "description": "Updated Description",
                        "body": "Updated Body"
                    }
                }
                """;

        mockMvc.perform(put("/api/articles/" + testArticle.getSlug())
                        .header("Authorization", testToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.article.title").value("Updated Title"))
                .andExpect(jsonPath("$.article.description").value("Updated Description"))
                .andExpect(jsonPath("$.article.body").value("Updated Body"));
    }

    @Test
    @DisplayName("PUT /api/articles/{slug} should return 401 without token")
    void whenUpdateArticleWithoutToken_thenShouldReturn401() throws Exception {
        String updateJson =
                """
                {
                    "article": {
                        "title": "Updated Title"
                    }
                }
                """;

        mockMvc.perform(put("/api/articles/" + testArticle.getSlug())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("PUT /api/articles/{slug} should return 400 when non-author tries to update")
    void whenNonAuthorUpdatesArticle_thenShouldReturn400() throws Exception {
        var otherRegistry = new UserRegistry("other@example.com", "otheruser", "password123");
        var otherUser = userService.signup(otherRegistry);
        var otherToken = "Token " + authTokenProvider.createAuthToken(otherUser);

        String updateJson =
                """
                {
                    "article": {
                        "title": "Updated Title"
                    }
                }
                """;

        mockMvc.perform(put("/api/articles/" + testArticle.getSlug())
                        .header("Authorization", otherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("DELETE /api/articles/{slug} should delete article")
    void whenDeleteArticle_thenShouldDeleteArticle() throws Exception {
        mockMvc.perform(delete("/api/articles/" + testArticle.getSlug()).header("Authorization", testToken))
                .andExpect(status().isOk());

        // Verify deletion
        mockMvc.perform(get("/api/articles/" + testArticle.getSlug())).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/articles/{slug} should return 401 without token")
    void whenDeleteArticleWithoutToken_thenShouldReturn401() throws Exception {
        mockMvc.perform(delete("/api/articles/" + testArticle.getSlug())).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("DELETE /api/articles/{slug} should return 400 when non-author tries to delete")
    void whenNonAuthorDeletesArticle_thenShouldReturn400() throws Exception {
        var otherRegistry = new UserRegistry("other@example.com", "otheruser", "password123");
        var otherUser = userService.signup(otherRegistry);
        var otherToken = "Token " + authTokenProvider.createAuthToken(otherUser);

        mockMvc.perform(delete("/api/articles/" + testArticle.getSlug()).header("Authorization", otherToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/articles/feed should return feed for authenticated user")
    void whenGetFeed_thenShouldReturnFeed() throws Exception {
        mockMvc.perform(get("/api/articles/feed").header("Authorization", testToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.articles").isArray())
                .andExpect(jsonPath("$.articlesCount").exists());
    }
}
