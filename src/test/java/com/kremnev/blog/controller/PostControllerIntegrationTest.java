package com.kremnev.blog.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kremnev.blog.config.TestConfiguration;
import com.kremnev.blog.dto.Request.CreatePostRequest;
import com.kremnev.blog.dto.Request.UpdatePostRequest;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringJUnitWebConfig(TestConfiguration.class)
@Transactional
@DisplayName("PostController Integration Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PostControllerIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        jdbcTemplate.execute("DELETE FROM post_tags");
        jdbcTemplate.execute("DELETE FROM comments");
        jdbcTemplate.execute("DELETE FROM tags");
        jdbcTemplate.execute("DELETE FROM posts");
    }

    @Test
    @DisplayName("Should create post with tags")
    void testCreatePost() throws Exception {
        CreatePostRequest request = new CreatePostRequest(
                "Test Title",
                "Test Content",
                List.of("java", "spring")
        );

        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.title").value("Test Title"))
                .andExpect(jsonPath("$.text").value("Test Content"))
                .andExpect(jsonPath("$.tags", hasSize(2)))
                .andExpect(jsonPath("$.tags", hasItems("java", "spring")))
                .andExpect(jsonPath("$.likesCount").value(0))
                .andExpect(jsonPath("$.commentsCount").value(0));
    }

    @Test
    @DisplayName("Should get all posts with pagination")
    void testGetAllPosts() throws Exception {
        // Create test posts
        createTestPost("Post 1", "Content 1", List.of("java"));
        createTestPost("Post 2", "Content 2", List.of("spring"));
        createTestPost("Post 3", "Content 3", List.of("testing"));

        mockMvc.perform(get("/api/posts")
                        .param("pageNumber", "1")
                        .param("pageSize", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.posts", hasSize(2)))
                .andExpect(jsonPath("$.hasNext").value(true))
                .andExpect(jsonPath("$.hasPrev").value(false));
    }

    @Test
    @DisplayName("Should search posts by title")
    void testSearchPostsByTitle() throws Exception {
        createTestPost("Java Tutorial", "Content 1", List.of());
        createTestPost("Spring Guide", "Content 2", List.of());

        mockMvc.perform(get("/api/posts")
                        .param("search", "java"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.posts", hasSize(1)))
                .andExpect(jsonPath("$.posts[0].title").value("Java Tutorial"));
    }

    @Test
    @DisplayName("Should get post by id")
    void testGetPostById() throws Exception {
        Long postId = createTestPost("Test Post", "Content", List.of("tag1"));

        mockMvc.perform(get("/api/posts/" + postId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(postId))
                .andExpect(jsonPath("$.title").value("Test Post"))
                .andExpect(jsonPath("$.text").value("Content"));
    }

    @Test
    @DisplayName("Should return 404 when post not found")
    void testGetPostByIdNotFound() throws Exception {
        mockMvc.perform(get("/api/posts/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 404 when updating non-existent post")
    void testUpdatePostNotFound() throws Exception {
        UpdatePostRequest request = new UpdatePostRequest(999L, "Title", "Content", List.of());

        mockMvc.perform(put("/api/posts/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should delete post")
    void testDeletePost() throws Exception {
        Long postId = createTestPost("To Delete", "Content", List.of());

        mockMvc.perform(delete("/api/posts/" + postId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/posts/" + postId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 404 when deleting non-existent post")
    void testDeletePostNotFound() throws Exception {
        mockMvc.perform(delete("/api/posts/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 404 when adding like to non-existent post")
    void testAddLikeNotFound() throws Exception {
        mockMvc.perform(post("/api/posts/999/likes"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should include comments count in response")
    void testCommentsCount() throws Exception {
        Long postId = createTestPost("Test Post", "Content", List.of());

        jdbcTemplate.update("INSERT INTO comments (post_id, text) VALUES (?, ?)", postId, "Comment 1");
        jdbcTemplate.update("INSERT INTO comments (post_id, text) VALUES (?, ?)", postId, "Comment 2");

        mockMvc.perform(get("/api/posts/" + postId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.commentsCount").value(2));
    }

    @Test
    @DisplayName("Should handle pagination boundaries")
    void testPaginationBoundaries() throws Exception {
        createTestPost("Post 1", "Content", List.of());

        mockMvc.perform(get("/api/posts")
                        .param("pageNumber", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hasNext").value(false))
                .andExpect(jsonPath("$.hasPrev").value(false));
    }

    private Long createTestPost(String title, String content, List<String> tags) {
        org.springframework.jdbc.support.KeyHolder postKeyHolder = new org.springframework.jdbc.support.GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            java.sql.PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO posts (title, text, likes_count) VALUES (?, ?, 0)",
                java.sql.Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, title);
            ps.setString(2, content);
            return ps;
        }, postKeyHolder);

        Long postId = ((Number) postKeyHolder.getKeys().get("id")).longValue();

        for (String tag : tags) {
            List<Long> existingTags = jdbcTemplate.query(
                "SELECT id FROM tags WHERE LOWER(name) = ?",
                (rs, rowNum) -> rs.getLong("id"),
                tag.toLowerCase()
            );

            Long tagId;
            if (!existingTags.isEmpty()) {
                tagId = existingTags.get(0);
            } else {
                org.springframework.jdbc.support.KeyHolder tagKeyHolder = new org.springframework.jdbc.support.GeneratedKeyHolder();
                jdbcTemplate.update(connection -> {
                    java.sql.PreparedStatement ps = connection.prepareStatement(
                        "INSERT INTO tags (name) VALUES (?)",
                        java.sql.Statement.RETURN_GENERATED_KEYS
                    );
                    ps.setString(1, tag.toLowerCase());
                    return ps;
                }, tagKeyHolder);
                tagId = ((Number) tagKeyHolder.getKeys().get("id")).longValue();
            }

            jdbcTemplate.update(
                    "INSERT INTO post_tags (post_id, tag_id) VALUES (?, ?)",
                    postId,
                    tagId
            );
        }

        return postId;
    }
}
