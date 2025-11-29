package com.kremnev.blog.repository;

import com.kremnev.blog.config.RepositoryTestConfiguration;
import com.kremnev.blog.model.Post;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringJUnitConfig(RepositoryTestConfiguration.class)
@Transactional
@DisplayName("PostRepository Integration Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PostRepositoryImplIntegrationTest {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        // Clean database before each test
        jdbcTemplate.execute("DELETE FROM post_tags");
        jdbcTemplate.execute("DELETE FROM comments");
        jdbcTemplate.execute("DELETE FROM tags");
        jdbcTemplate.execute("DELETE FROM posts");
    }

    @Test
    @DisplayName("Should create post with tags")
    void testCreateWithTags() {
        List<String> tags = List.of("java", "spring", "testing");

        Post created = postRepository.create("Test Title", "Test Content", tags);

        assertNotNull(created);
        assertNotNull(created.getId());
        assertEquals("Test Title", created.getTitle());
        assertEquals("Test Content", created.getText());
        assertEquals(0, created.getLikesCount());
        assertEquals(3, created.getTags().size());
        assertTrue(created.getTags().containsAll(tags));
    }

    @Test
    @DisplayName("Should create post without tags")
    void testCreateWithoutTags() {
        Post created = postRepository.create("Title", "Content", List.of());

        assertNotNull(created);
        assertTrue(created.getTags().isEmpty());
    }

    @Test
    @DisplayName("Should find post by id with tags")
    void testFindByIdWithTags() {
        Post created = postRepository.create("Test", "Content", List.of("java", "spring"));

        Optional<Post> found = postRepository.findById(created.getId());

        assertTrue(found.isPresent());
        assertEquals(created.getId(), found.get().getId());
        assertEquals("Test", found.get().getTitle());
        assertEquals(2, found.get().getTags().size());
        assertTrue(found.get().getTags().contains("java"));
        assertTrue(found.get().getTags().contains("spring"));
    }

    @Test
    @DisplayName("Should return empty optional when post not found")
    void testFindByIdNotFound() {
        Optional<Post> found = postRepository.findById(999L);

        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("Should find all posts with pagination")
    void testFindAllWithPagination() {
        postRepository.create("Post 1", "Content 1", List.of("java"));
        postRepository.create("Post 2", "Content 2", List.of("spring"));
        postRepository.create("Post 3", "Content 3", List.of("testing"));

        Pair<List<Post>, Integer> result = postRepository.findAll(null, 1, 2);

        assertNotNull(result);
        assertEquals(2, result.getFirst().size());
        assertEquals(3, result.getSecond());
    }

    @Test
    @DisplayName("Should search posts by title")
    void testSearchByTitle() {
        postRepository.create("Java Tutorial", "Content 1", List.of());
        postRepository.create("Spring Boot Guide", "Content 2", List.of());
        postRepository.create("Testing Guide", "Content 3", List.of());

        Pair<List<Post>, Integer> result = postRepository.findAll("java", 1, 10);

        assertEquals(1, result.getFirst().size());
        assertEquals(1, result.getSecond());
        assertEquals("Java Tutorial", result.getFirst().get(0).getTitle());
    }

    @Test
    @DisplayName("Should update post")
    void testUpdate() {
        Post created = postRepository.create("Original Title", "Original Content", List.of("old"));

        Optional<Post> updated = postRepository.update(
                created.getId(),
                "Updated Title",
                "Updated Content",
                List.of("new", "tags")
        );

        assertTrue(updated.isPresent());
        assertEquals("Updated Title", updated.get().getTitle());
        assertEquals("Updated Content", updated.get().getText());
        assertEquals(2, updated.get().getTags().size());
        assertTrue(updated.get().getTags().contains("new"));
        assertTrue(updated.get().getTags().contains("tags"));
        assertFalse(updated.get().getTags().contains("old"));
    }

    @Test
    @DisplayName("Should return empty optional when updating non-existent post")
    void testUpdateNonExistent() {
        Optional<Post> updated = postRepository.update(999L, "Title", "Content", List.of());

        assertFalse(updated.isPresent());
    }

    @Test
    @DisplayName("Should delete post")
    void testDelete() {
        Post created = postRepository.create("To Delete", "Content", List.of("tag"));

        boolean deleted = postRepository.delete(created.getId());

        assertTrue(deleted);
        Optional<Post> found = postRepository.findById(created.getId());
        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("Should return false when deleting non-existent post")
    void testDeleteNonExistent() {
        boolean deleted = postRepository.delete(999L);

        assertFalse(deleted);
    }

    @Test
    @DisplayName("Should increment likes count")
    void testAddLike() {
        Post created = postRepository.create("Post", "Content", List.of());
        assertEquals(0, created.getLikesCount());

        Optional<Post> liked = postRepository.addLike(created.getId());

        assertTrue(liked.isPresent());
        assertEquals(1, liked.get().getLikesCount());

        Optional<Post> likedAgain = postRepository.addLike(created.getId());
        assertTrue(likedAgain.isPresent());
        assertEquals(2, likedAgain.get().getLikesCount());
    }

    @Test
    @DisplayName("Should return empty optional when adding like to non-existent post")
    void testAddLikeNonExistent() {
        Optional<Post> result = postRepository.addLike(999L);

        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Should handle duplicate tags correctly")
    void testDuplicateTags() {
        Post created = postRepository.create("Post", "Content", List.of("java", "java", "spring", "JAVA"));

        Optional<Post> found = postRepository.findById(created.getId());
        assertTrue(found.isPresent());
        assertEquals(2, found.get().getTags().size());
        assertTrue(found.get().getTags().contains("java"));
        assertTrue(found.get().getTags().contains("spring"));
    }

    @Test
    @DisplayName("Should count comments correctly in findById")
    void testCommentsCountInFindById() {
        Post created = postRepository.create("Post", "Content", List.of());

        jdbcTemplate.update("INSERT INTO comments (post_id, text) VALUES (?, ?)", created.getId(), "Comment 1");
        jdbcTemplate.update("INSERT INTO comments (post_id, text) VALUES (?, ?)", created.getId(), "Comment 2");

        Optional<Post> found = postRepository.findById(created.getId());
        assertTrue(found.isPresent());
        assertEquals(2, found.get().getCommentsCount());
    }

    @Test
    @DisplayName("Should count comments correctly in findAll")
    void testCommentsCountInFindAll() {
        Post post1 = postRepository.create("Post 1", "Content", List.of());
        Post post2 = postRepository.create("Post 2", "Content", List.of());

        jdbcTemplate.update("INSERT INTO comments (post_id, text) VALUES (?, ?)", post1.getId(), "Comment");
        jdbcTemplate.update("INSERT INTO comments (post_id, text) VALUES (?, ?)", post2.getId(), "Comment 1");
        jdbcTemplate.update("INSERT INTO comments (post_id, text) VALUES (?, ?)", post2.getId(), "Comment 2");

        Pair<List<Post>, Integer> result = postRepository.findAll(null, 1, 10);

        Post foundPost1 = result.getFirst().stream()
                .filter(p -> p.getId().equals(post1.getId()))
                .findFirst()
                .orElseThrow();
        Post foundPost2 = result.getFirst().stream()
                .filter(p -> p.getId().equals(post2.getId()))
                .findFirst()
                .orElseThrow();

        assertEquals(1, foundPost1.getCommentsCount());
        assertEquals(2, foundPost2.getCommentsCount());
    }
}
