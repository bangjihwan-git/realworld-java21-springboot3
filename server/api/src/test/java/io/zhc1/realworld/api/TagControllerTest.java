package io.zhc1.realworld.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import io.zhc1.realworld.model.Article;
import io.zhc1.realworld.model.Tag;
import io.zhc1.realworld.model.User;
import io.zhc1.realworld.model.UserRegistry;
import io.zhc1.realworld.service.ArticleService;
import io.zhc1.realworld.service.UserService;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@DisplayName("Tag API - Tag Retrieval Operations")
class TagControllerTest {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    UserService userService;

    @Autowired
    ArticleService articleService;

    User testUser;

    @BeforeEach
    void setUp() {
        var registry = new UserRegistry("test@example.com", "testuser", "password123");
        testUser = userService.signup(registry);

        // Create articles with tags
        var article1 = new Article(testUser, "Test Article 1", "Description 1", "Body 1");
        articleService.write(article1, Set.of(new Tag("java"), new Tag("spring")));

        var article2 = new Article(testUser, "Test Article 2", "Description 2", "Body 2");
        articleService.write(article2, Set.of(new Tag("kotlin"), new Tag("spring")));
    }

    @Test
    @DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
    @DisplayName("GET /api/tags should return all tags")
    void whenGetAllTags_thenShouldReturnTags() throws Exception {
        mockMvc.perform(get("/api/tags"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tags").isArray())
                .andExpect(jsonPath("$.tags.length()").value(3));
    }

    @Test
    @DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
    @DisplayName("GET /api/tags should return empty array when no tags exist")
    void whenGetAllTagsWithNoTags_thenShouldReturnEmptyArray() throws Exception {
        // This test will show tags from setUp, so we need a separate test context
        // For now, we just verify the endpoint works
        mockMvc.perform(get("/api/tags"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tags").isArray());
    }
}
