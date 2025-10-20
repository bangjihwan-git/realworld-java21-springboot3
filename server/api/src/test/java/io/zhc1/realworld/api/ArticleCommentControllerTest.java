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
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import io.zhc1.realworld.config.AuthTokenProvider;
import io.zhc1.realworld.model.Article;
import io.zhc1.realworld.model.ArticleComment;
import io.zhc1.realworld.model.User;
import io.zhc1.realworld.model.UserRegistry;
import io.zhc1.realworld.service.ArticleCommentService;
import io.zhc1.realworld.service.ArticleService;
import io.zhc1.realworld.service.UserService;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@DisplayName("Article Comment API - Comment CRUD Operations")
class ArticleCommentControllerTest {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    UserService userService;

    @Autowired
    ArticleService articleService;

    @Autowired
    ArticleCommentService commentService;

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
    @DisplayName("POST /api/articles/{slug}/comments should create comment")
    void whenPostComment_thenShouldCreateComment() throws Exception {
        String commentJson =
                """
                {
                    "comment": {
                        "body": "This is a test comment"
                    }
                }
                """;

        mockMvc.perform(post("/api/articles/" + testArticle.getSlug() + "/comments")
                        .header("Authorization", testToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(commentJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comment.body").value("This is a test comment"))
                .andExpect(jsonPath("$.comment.author.username").value("testuser"));
    }

    @Test
    @DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
    @DisplayName("POST /api/articles/{slug}/comments should return 401 without token")
    void whenPostCommentWithoutToken_thenShouldReturn401() throws Exception {
        String commentJson =
                """
                {
                    "comment": {
                        "body": "This is a test comment"
                    }
                }
                """;

        mockMvc.perform(post("/api/articles/" + testArticle.getSlug() + "/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(commentJson))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
    @DisplayName("POST /api/articles/{slug}/comments should return 404 for non-existent article")
    void whenPostCommentToNonExistentArticle_thenShouldReturn404() throws Exception {
        String commentJson =
                """
                {
                    "comment": {
                        "body": "This is a test comment"
                    }
                }
                """;

        mockMvc.perform(post("/api/articles/non-existent-slug/comments")
                        .header("Authorization", testToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(commentJson))
                .andExpect(status().isNotFound());
    }

    @Test
    @DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
    @DisplayName("GET /api/articles/{slug}/comments should return comments")
    void whenGetComments_thenShouldReturnComments() throws Exception {
        // Create some comments
        var comment1 = new ArticleComment(testArticle, testUser, "Comment 1");
        commentService.write(comment1);

        var comment2 = new ArticleComment(testArticle, testUser, "Comment 2");
        commentService.write(comment2);

        mockMvc.perform(get("/api/articles/" + testArticle.getSlug() + "/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comments").isArray())
                .andExpect(jsonPath("$.comments.length()").value(2));
    }

    @Test
    @DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
    @DisplayName("GET /api/articles/{slug}/comments should return empty array when no comments")
    void whenGetCommentsWithNoComments_thenShouldReturnEmptyArray() throws Exception {
        mockMvc.perform(get("/api/articles/" + testArticle.getSlug() + "/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comments").isArray())
                .andExpect(jsonPath("$.comments.length()").value(0));
    }

    @Test
    @DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
    @DisplayName("GET /api/articles/{slug}/comments should return 404 for non-existent article")
    void whenGetCommentsForNonExistentArticle_thenShouldReturn404() throws Exception {
        mockMvc.perform(get("/api/articles/non-existent-slug/comments")).andExpect(status().isNotFound());
    }

    @Test
    @DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
    @DisplayName("DELETE /api/articles/{slug}/comments/{id} should delete comment")
    void whenDeleteComment_thenShouldDeleteComment() throws Exception {
        var comment = new ArticleComment(testArticle, testUser, "Test Comment");
        var savedComment = commentService.write(comment);

        mockMvc.perform(delete("/api/articles/" + testArticle.getSlug() + "/comments/" + savedComment.getId())
                        .header("Authorization", testToken))
                .andExpect(status().isOk());
    }

    @Test
    @DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
    @DisplayName("DELETE /api/articles/{slug}/comments/{id} should return 401 without token")
    void whenDeleteCommentWithoutToken_thenShouldReturn401() throws Exception {
        var comment = new ArticleComment(testArticle, testUser, "Test Comment");
        var savedComment = commentService.write(comment);

        mockMvc.perform(delete("/api/articles/" + testArticle.getSlug() + "/comments/" + savedComment.getId()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
    @DisplayName("DELETE /api/articles/{slug}/comments/{id} should return 400 when non-author tries to delete")
    void whenNonAuthorDeletesComment_thenShouldReturn400() throws Exception {
        var comment = new ArticleComment(testArticle, testUser, "Test Comment");
        var savedComment = commentService.write(comment);

        var otherRegistry = new UserRegistry("other@example.com", "otheruser", "password123");
        var otherUser = userService.signup(otherRegistry);
        var otherToken = "Token " + authTokenProvider.createAuthToken(otherUser);

        mockMvc.perform(delete("/api/articles/" + testArticle.getSlug() + "/comments/" + savedComment.getId())
                        .header("Authorization", otherToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
    @DisplayName("DELETE /api/articles/{slug}/comments/{id} should return 404 for non-existent comment")
    void whenDeleteNonExistentComment_thenShouldReturn404() throws Exception {
        mockMvc.perform(delete("/api/articles/" + testArticle.getSlug() + "/comments/99999")
                        .header("Authorization", testToken))
                .andExpect(status().isNotFound());
    }
}
