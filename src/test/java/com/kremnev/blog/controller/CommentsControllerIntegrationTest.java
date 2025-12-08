package com.kremnev.blog.controller;

import com.kremnev.blog.dto.Request.CreateCommentRequest;
import com.kremnev.blog.dto.Request.UpdateCommentRequest;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.ObjectMapper;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("CommentsController Integration Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CommentsControllerIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Long testPostId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        jdbcTemplate.execute("DELETE FROM post_tags");
        jdbcTemplate.execute("DELETE FROM comments");
        jdbcTemplate.execute("DELETE FROM tags");
        jdbcTemplate.execute("DELETE FROM posts");

        org.springframework.jdbc.support.KeyHolder keyHolder = new org.springframework.jdbc.support.GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            java.sql.PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO posts (title, text, likes_count) VALUES ('Test Post', 'Content', 0)",
                java.sql.Statement.RETURN_GENERATED_KEYS
            );
            return ps;
        }, keyHolder);
        testPostId = ((Number) keyHolder.getKeys().get("id")).longValue();
    }

    @Test
    @DisplayName("Should get all comments for a post")
    void testGetAllComments() throws Exception {
        createTestComment(testPostId, "Comment 1");
        createTestComment(testPostId, "Comment 2");
        createTestComment(testPostId, "Comment 3");

        mockMvc.perform(get("/api/posts/" + testPostId + "/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[*].text", containsInAnyOrder("Comment 1", "Comment 2", "Comment 3")));
    }

    @Test
    @DisplayName("Should return empty list when post has no comments")
    void testGetAllCommentsEmpty() throws Exception {
        mockMvc.perform(get("/api/posts/" + testPostId + "/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("Should get comment by id")
    void testGetCommentById() throws Exception {
        Long commentId = createTestComment(testPostId, "Test comment");

        mockMvc.perform(get("/api/posts/" + testPostId + "/comments/" + commentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(commentId))
                .andExpect(jsonPath("$.text").value("Test comment"))
                .andExpect(jsonPath("$.postId").value(testPostId));
    }

    @Test
    @DisplayName("Should return 404 when comment not found")
    void testGetCommentByIdNotFound() throws Exception {
        mockMvc.perform(get("/api/posts/" + testPostId + "/comments/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should update comment")
    void testUpdateComment() throws Exception {
        Long commentId = createTestComment(testPostId, "Original text");

        UpdateCommentRequest request = new UpdateCommentRequest(commentId, "Updated text", testPostId);

        mockMvc.perform(put("/api/posts/" + testPostId + "/comments/" + commentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.text").value("Updated text"))
                .andExpect(jsonPath("$.id").value(commentId));
    }

    @Test
    @DisplayName("Should return 404 when updating non-existent comment")
    void testUpdateCommentNotFound() throws Exception {
        UpdateCommentRequest request = new UpdateCommentRequest(999L, "Updated text", testPostId);

        mockMvc.perform(put("/api/posts/" + testPostId + "/comments/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 404 when updating comment with wrong post id")
    void testUpdateCommentWithWrongPostId() throws Exception {
        Long commentId = createTestComment(testPostId, "Text");
        UpdateCommentRequest request = new UpdateCommentRequest(commentId, "Updated", 999L);

        mockMvc.perform(put("/api/posts/999/comments/" + commentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should delete comment")
    void testDeleteComment() throws Exception {
        Long commentId = createTestComment(testPostId, "To delete");

        mockMvc.perform(delete("/api/posts/" + testPostId + "/comments/" + commentId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/posts/" + testPostId + "/comments/" + commentId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 404 when deleting non-existent comment")
    void testDeleteCommentNotFound() throws Exception {
        mockMvc.perform(delete("/api/posts/" + testPostId + "/comments/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 404 when deleting comment with wrong post id")
    void testDeleteCommentWithWrongPostId() throws Exception {
        Long commentId = createTestComment(testPostId, "Text");

        mockMvc.perform(delete("/api/posts/999/comments/" + commentId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should not return comments from other posts")
    void testCommentsIsolation() throws Exception {
        org.springframework.jdbc.support.KeyHolder keyHolder = new org.springframework.jdbc.support.GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            java.sql.PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO posts (title, text, likes_count) VALUES ('Other Post', 'Content', 0)",
                java.sql.Statement.RETURN_GENERATED_KEYS
            );
            return ps;
        }, keyHolder);
        Long otherPostId = ((Number) keyHolder.getKeys().get("id")).longValue();

        createTestComment(testPostId, "Comment for test post");
        createTestComment(otherPostId, "Comment for other post");

        mockMvc.perform(get("/api/posts/" + testPostId + "/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].text").value("Comment for test post"));
    }

    @Test
    @DisplayName("Should handle long comment text")
    void testLongCommentText() throws Exception {
        String longText = "This is a very long comment. ".repeat(100);
        CreateCommentRequest request = new CreateCommentRequest(testPostId, longText);

        mockMvc.perform(post("/api/posts/" + testPostId + "/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.text").value(longText));
    }

    @Test
    @DisplayName("Should create multiple comments for same post")
    void testMultipleComments() throws Exception {
        for (int i = 1; i <= 5; i++) {
            createTestComment(testPostId, "Comment " + i);
        }

        mockMvc.perform(get("/api/posts/" + testPostId + "/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(5)));
    }

    private Long createTestComment(Long postId, String text) {
        org.springframework.jdbc.support.KeyHolder keyHolder = new org.springframework.jdbc.support.GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            java.sql.PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO comments (post_id, text) VALUES (?, ?)",
                java.sql.Statement.RETURN_GENERATED_KEYS
            );
            ps.setLong(1, postId);
            ps.setString(2, text);
            return ps;
        }, keyHolder);

        return ((Number) keyHolder.getKeys().get("id")).longValue();
    }
}
