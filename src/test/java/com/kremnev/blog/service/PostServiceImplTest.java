package com.kremnev.blog.service;

import com.kremnev.blog.model.Post;
import com.kremnev.blog.model.PostsResponse;
import com.kremnev.blog.repository.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.util.Pair;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PostServiceImpl Tests")
class PostServiceImplTest {

    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private PostServiceImpl postService;

    private Post testPost;
    private List<String> testTags;

    @BeforeEach
    void setUp() {
        testTags = List.of("java", "spring", "testing");
        testPost = new Post(1L, "Test Title", "Test Content", testTags, 5, 3);
    }

    @Test
    @DisplayName("Should return posts response with correct pagination when getting all posts")
    void testGetAll() {
        List<Post> posts = List.of(testPost);
        int totalCount = 10;
        when(postRepository.findAll(anyString(), anyInt(), anyInt()))
                .thenReturn(Pair.of(posts, totalCount));

        PostsResponse result = postService.getAll("test", 1, 5);

        assertNotNull(result);
        assertEquals(1, result.getPosts().size());
        assertEquals(testPost, result.getPosts().get(0));
        verify(postRepository, times(1)).findAll("test", 1, 5);
    }

    @Test
    @DisplayName("Should return posts response with empty list when no posts found")
    void testGetAllWithNoPosts() {
        when(postRepository.findAll(anyString(), anyInt(), anyInt()))
                .thenReturn(Pair.of(List.of(), 0));

        PostsResponse result = postService.getAll("nonexistent", 1, 5);

        assertNotNull(result);
        assertTrue(result.getPosts().isEmpty());
        verify(postRepository, times(1)).findAll("nonexistent", 1, 5);
    }

    @Test
    @DisplayName("Should return posts response with null search parameter")
    void testGetAllWithNullSearch() {
        List<Post> posts = List.of(testPost);
        when(postRepository.findAll(isNull(), anyInt(), anyInt()))
                .thenReturn(Pair.of(posts, 1));

        PostsResponse result = postService.getAll(null, 1, 10);

        assertNotNull(result);
        assertEquals(1, result.getPosts().size());
        verify(postRepository, times(1)).findAll(null, 1, 10);
    }

    @Test
    @DisplayName("Should return post when found by id")
    void testGetById() {
        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));

        Optional<Post> result = postService.getById(1L);

        assertTrue(result.isPresent());
        assertEquals(testPost, result.get());
        assertEquals(1L, result.get().getId());
        assertEquals("Test Title", result.get().getTitle());
        verify(postRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should return empty optional when post not found by id")
    void testGetByIdNotFound() {
        when(postRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<Post> result = postService.getById(999L);

        assertFalse(result.isPresent());
        verify(postRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("Should create post with valid data")
    void testCreate() {
        when(postRepository.create(anyString(), anyString(), anyList()))
                .thenReturn(testPost);

        Post result = postService.create("Test Title", "Test Content", testTags);

        assertNotNull(result);
        assertEquals(testPost, result);
        assertEquals("Test Title", result.getTitle());
        assertEquals("Test Content", result.getText());
        assertEquals(testTags, result.getTags());
        verify(postRepository, times(1)).create("Test Title", "Test Content", testTags);
    }

    @Test
    @DisplayName("Should create post with empty tags list")
    void testCreateWithEmptyTags() {
        List<String> emptyTags = List.of();
        Post postWithNoTags = new Post(2L, "Title", "Content", emptyTags, 0, 0);
        when(postRepository.create(anyString(), anyString(), anyList()))
                .thenReturn(postWithNoTags);

        Post result = postService.create("Title", "Content", emptyTags);

        assertNotNull(result);
        assertTrue(result.getTags().isEmpty());
        verify(postRepository, times(1)).create("Title", "Content", emptyTags);
    }

    @Test
    @DisplayName("Should update post when post exists")
    void testUpdate() {
        Post updatedPost = new Post(1L, "Updated Title", "Updated Content", testTags, 5, 3);
        when(postRepository.update(anyLong(), anyString(), anyString(), anyList()))
                .thenReturn(Optional.of(updatedPost));

        Optional<Post> result = postService.update(1L, "Updated Title", "Updated Content", testTags);

        assertTrue(result.isPresent());
        assertEquals("Updated Title", result.get().getTitle());
        assertEquals("Updated Content", result.get().getText());
        verify(postRepository, times(1)).update(1L, "Updated Title", "Updated Content", testTags);
    }

    @Test
    @DisplayName("Should return empty optional when updating non-existent post")
    void testUpdateNonExistentPost() {
        when(postRepository.update(anyLong(), anyString(), anyString(), anyList()))
                .thenReturn(Optional.empty());

        Optional<Post> result = postService.update(999L, "Title", "Content", testTags);

        assertFalse(result.isPresent());
        verify(postRepository, times(1)).update(999L, "Title", "Content", testTags);
    }

    @Test
    @DisplayName("Should delete post when post exists")
    void testDelete() {
        when(postRepository.delete(1L)).thenReturn(true);

        boolean result = postService.delete(1L);

        assertTrue(result);
        verify(postRepository, times(1)).delete(1L);
    }

    @Test
    @DisplayName("Should return false when deleting non-existent post")
    void testDeleteNonExistentPost() {
        when(postRepository.delete(999L)).thenReturn(false);

        boolean result = postService.delete(999L);

        assertFalse(result);
        verify(postRepository, times(1)).delete(999L);
    }

    @Test
    @DisplayName("Should add like and return updated post")
    void testAddLike() {
        Post likedPost = new Post(1L, "Test Title", "Test Content", testTags, 6, 3);
        when(postRepository.addLike(1L)).thenReturn(Optional.of(likedPost));

        Optional<Post> result = postService.addLike(1L);

        assertTrue(result.isPresent());
        assertEquals(6, result.get().getLikesCount());
        verify(postRepository, times(1)).addLike(1L);
    }

    @Test
    @DisplayName("Should return empty optional when adding like to non-existent post")
    void testAddLikeToNonExistentPost() {
        when(postRepository.addLike(999L)).thenReturn(Optional.empty());

        Optional<Post> result = postService.addLike(999L);

        assertFalse(result.isPresent());
        verify(postRepository, times(1)).addLike(999L);
    }

    @Test
    @DisplayName("Should verify repository is called exactly once for each operation")
    void testRepositoryInteractions() {
        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));
        when(postRepository.create(anyString(), anyString(), anyList())).thenReturn(testPost);
        when(postRepository.delete(1L)).thenReturn(true);

        postService.getById(1L);
        postService.create("Title", "Content", testTags);
        postService.delete(1L);

        verify(postRepository, times(1)).findById(1L);
        verify(postRepository, times(1)).create("Title", "Content", testTags);
        verify(postRepository, times(1)).delete(1L);
        verifyNoMoreInteractions(postRepository);
    }
}
