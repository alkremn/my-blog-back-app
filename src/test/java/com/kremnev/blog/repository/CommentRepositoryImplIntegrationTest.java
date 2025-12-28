package com.kremnev.blog.repository;

import com.kremnev.blog.model.Comment;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("CommentRepository Integration Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CommentRepositoryImplIntegrationTest {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Long testPostId;

    @BeforeEach
    void setUp() {
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
    @DisplayName("Should create comment")
    void testCreate() {
        Comment created = commentRepository.create(testPostId, "Test comment text");

        assertNotNull(created);
        assertNotNull(created.getId());
        assertEquals(testPostId, created.getPostId());
        assertEquals("Test comment text", created.getText());
        assertNotNull(created.getCreatedAt());
        assertNotNull(created.getUpdatedAt());
    }

    @Test
    @DisplayName("Should find comment by id")
    void testFindById() {
        Comment created = commentRepository.create(testPostId, "Test comment");

        Optional<Comment> found = commentRepository.findById(created.getId());

        assertTrue(found.isPresent());
        assertEquals(created.getId(), found.get().getId());
        assertEquals("Test comment", found.get().getText());
        assertEquals(testPostId, found.get().getPostId());
    }

    @Test
    @DisplayName("Should return empty optional when comment not found")
    void testFindByIdNotFound() {
        Optional<Comment> found = commentRepository.findById(999L);

        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("Should find all comments by post id")
    void testFindAllByPostId() {
        commentRepository.create(testPostId, "Comment 1");
        commentRepository.create(testPostId, "Comment 2");
        commentRepository.create(testPostId, "Comment 3");

        List<Comment> comments = commentRepository.findAllByPostId(testPostId);

        assertEquals(3, comments.size());
        assertTrue(comments.stream().allMatch(c -> c.getPostId().equals(testPostId)));
    }

    @Test
    @DisplayName("Should return empty list when post has no comments")
    void testFindAllByPostIdEmpty() {
        List<Comment> comments = commentRepository.findAllByPostId(testPostId);

        assertNotNull(comments);
        assertTrue(comments.isEmpty());
    }

    @Test
    @DisplayName("Should not return comments from other posts")
    void testFindAllByPostIdIsolation() {
        org.springframework.jdbc.support.KeyHolder keyHolder = new org.springframework.jdbc.support.GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            java.sql.PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO posts (title, text, likes_count) VALUES ('Other Post', 'Content', 0)",
                java.sql.Statement.RETURN_GENERATED_KEYS
            );
            return ps;
        }, keyHolder);
        Long otherPostId = ((Number) keyHolder.getKeys().get("id")).longValue();

        commentRepository.create(testPostId, "Comment for test post");
        commentRepository.create(otherPostId, "Comment for other post");

        List<Comment> comments = commentRepository.findAllByPostId(testPostId);

        assertEquals(1, comments.size());
        assertEquals("Comment for test post", comments.get(0).getText());
    }

    @Test
    @DisplayName("Should update comment")
    void testUpdate() {
        Comment created = commentRepository.create(testPostId, "Original text");

        Optional<Comment> updated = commentRepository.update(
                created.getId(),
                testPostId,
                "Updated text"
        );

        assertTrue(updated.isPresent());
        assertEquals("Updated text", updated.get().getText());
        assertEquals(created.getId(), updated.get().getId());
        assertNotNull(updated.get().getUpdatedAt());
    }

    @Test
    @DisplayName("Should return empty optional when updating non-existent comment")
    void testUpdateNonExistent() {
        Optional<Comment> updated = commentRepository.update(999L, testPostId, "Text");

        assertFalse(updated.isPresent());
    }

    @Test
    @DisplayName("Should not update comment with wrong post id")
    void testUpdateWithWrongPostId() {
        Comment created = commentRepository.create(testPostId, "Text");

        Optional<Comment> updated = commentRepository.update(created.getId(), 999L, "Updated");

        assertFalse(updated.isPresent());

        Optional<Comment> original = commentRepository.findById(created.getId());
        assertTrue(original.isPresent());
        assertEquals("Text", original.get().getText());
    }

    @Test
    @DisplayName("Should delete comment")
    void testDelete() {
        Comment created = commentRepository.create(testPostId, "To delete");

        boolean deleted = commentRepository.delete(created.getId(), testPostId);

        assertTrue(deleted);
        Optional<Comment> found = commentRepository.findById(created.getId());
        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("Should return false when deleting non-existent comment")
    void testDeleteNonExistent() {
        boolean deleted = commentRepository.delete(999L, testPostId);

        assertFalse(deleted);
    }

    @Test
    @DisplayName("Should not delete comment with wrong post id")
    void testDeleteWithWrongPostId() {
        Comment created = commentRepository.create(testPostId, "Text");

        boolean deleted = commentRepository.delete(created.getId(), 999L);

        assertFalse(deleted);

        Optional<Comment> found = commentRepository.findById(created.getId());
        assertTrue(found.isPresent());
    }

    @Test
    @DisplayName("Should cascade delete comments when post is deleted")
    void testCascadeDelete() {
        Comment comment1 = commentRepository.create(testPostId, "Comment 1");
        Comment comment2 = commentRepository.create(testPostId, "Comment 2");

        jdbcTemplate.update("DELETE FROM posts WHERE id = ?", testPostId);

        Optional<Comment> found1 = commentRepository.findById(comment1.getId());
        Optional<Comment> found2 = commentRepository.findById(comment2.getId());

        assertFalse(found1.isPresent());
        assertFalse(found2.isPresent());
    }

    @Test
    @DisplayName("Should handle long comment text")
    void testLongCommentText() {
        String longText = "This is a very long comment. ".repeat(100);

        Comment created = commentRepository.create(testPostId, longText);

        assertNotNull(created);
        assertEquals(longText, created.getText());

        Optional<Comment> found = commentRepository.findById(created.getId());
        assertTrue(found.isPresent());
        assertEquals(longText, found.get().getText());
    }

    @Test
    @DisplayName("Should create multiple comments for same post")
    void testMultipleCommentsForSamePost() {
        for (int i = 1; i <= 5; i++) {
            commentRepository.create(testPostId, "Comment " + i);
        }

        List<Comment> comments = commentRepository.findAllByPostId(testPostId);

        assertEquals(5, comments.size());
        assertTrue(comments.stream().allMatch(c -> c.getPostId().equals(testPostId)));
    }
}
